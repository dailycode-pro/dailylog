# Kotlin Coding Standards for Libraries

## Visibility Rules

### The Golden Rule
**Everything is `private` or `internal` by default. Make `public` only what's documented and tested.**

```kotlin
// ✅ Internal by default, expose deliberately
internal class TooltipRendererImpl : TooltipRenderer { ... }

public interface TooltipRenderer {
    public fun render(config: TooltipConfig)
}
```

### Visibility Decision Tree

1. Is it part of the public API contract? → `public`
2. Is it shared between modules? → `public` in api module, `internal` otherwise
3. Is it shared within the module? → `internal`
4. Is it used only in this file/class? → `private`

---

## Immutability

### Public API Types

```kotlin
// ✅ Immutable data class
public data class TooltipConfig(
    val text: String,
    val position: Position = Position.Bottom,
    val style: TooltipStyle = TooltipStyle.Default,
)

// ✅ Return immutable collections
public fun getSteps(): List<Step> = steps.toList()

// ❌ NEVER expose mutable state
public val items: MutableList<Item> // NO!
```

### Builder Pattern for Complex Objects

```kotlin
public class TooltipConfig private constructor(
    public val text: String,
    public val position: Position,
    public val style: TooltipStyle,
) {
    public class Builder {
        public var text: String = ""
        public var position: Position = Position.Bottom
        public var style: TooltipStyle = TooltipStyle.Default
        
        public fun build(): TooltipConfig {
            require(text.isNotBlank()) { "text must not be blank" }
            return TooltipConfig(text, position, style)
        }
    }
    
    public companion object {
        public inline fun build(block: Builder.() -> Unit): TooltipConfig =
            Builder().apply(block).build()
    }
}
```

---

## Coroutines Best Practices

### Suspend Functions

```kotlin
// ✅ Let the caller control the dispatcher
public suspend fun loadData(): Data {
    // Don't wrap in withContext unless you KNOW it's IO-bound
    return parseResponse(response)
}

// ✅ Explicitly IO-bound operation
public suspend fun readFile(path: String): ByteArray =
    withContext(Dispatchers.IO) {
        File(path).readBytes()
    }

// ❌ Don't create your own scope silently
public fun startSync() {
    CoroutineScope(Dispatchers.IO).launch { // Leak! Uncontrolled lifecycle
        // ...
    }
}

// ✅ Accept scope from caller
public fun startSync(scope: CoroutineScope): Job =
    scope.launch {
        // ...
    }
```

### Flow APIs

```kotlin
// ✅ Cold Flow — computed lazily
public fun observeChanges(): Flow<List<Item>> = flow {
    // ...
}

// ✅ StateFlow for state holders
public interface DataStore {
    public val state: StateFlow<State>
}

// ✅ Use callbackFlow for wrapping callback-based APIs
public fun observeLocationUpdates(): Flow<Location> = callbackFlow {
    val callback = object : LocationCallback {
        override fun onLocation(location: Location) {
            trySend(location)
        }
    }
    registerCallback(callback)
    awaitClose { unregisterCallback(callback) }
}
```

### Structured Concurrency

```kotlin
// ✅ Cancelation-safe
public suspend fun fetchAllData(): CombinedData = coroutineScope {
    val users = async { fetchUsers() }
    val posts = async { fetchPosts() }
    CombinedData(users.await(), posts.await())
}
```

---

## Error Handling

### Exceptions vs Result

```kotlin
// Use exceptions for programming errors (bugs)
public fun process(input: String): Output {
    require(input.isNotBlank()) { "input must not be blank" } // IllegalArgumentException
    check(isInitialized) { "must call initialize() first" }   // IllegalStateException
    // ...
}

// Use Result or sealed types for expected failures
public sealed interface FetchResult {
    public data class Success(val data: Data) : FetchResult
    public data class Error(val message: String, val code: Int) : FetchResult
}

// Or use kotlin.Result
public suspend fun tryFetch(): Result<Data>
```

### Library Exception Hierarchy

```kotlin
/**
 * Base exception for all MyLibrary errors.
 */
public open class MyLibraryException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Thrown when library configuration is invalid.
 */
public class InvalidConfigException(
    message: String,
) : MyLibraryException(message)

/**
 * Thrown when a required resource is not found.
 */
public class ResourceNotFoundException(
    public val resourceId: String,
) : MyLibraryException("Resource not found: $resourceId")
```

---

## Compose-Specific Standards

### State Management

```kotlin
// ✅ State hoisting — state up, events down
@Composable
public fun TooltipHost(
    state: TooltipHostState = rememberTooltipHostState(),
    modifier: Modifier = Modifier,
    content: @Composable TooltipHostScope.() -> Unit,
) {
    // Implementation
}

// ✅ Stable annotation for state classes
@Stable
public class TooltipHostState {
    internal var currentTooltip by mutableStateOf<TooltipData?>(null)
        private set
    
    public val isVisible: Boolean get() = currentTooltip != null
    
    public suspend fun show(text: String, position: Position): Boolean {
        // Suspend until dismissed
    }
    
    public fun dismiss() {
        currentTooltip = null
    }
}

@Composable
public fun rememberTooltipHostState(): TooltipHostState {
    return remember { TooltipHostState() }
}
```

### Performance

```kotlin
// ✅ Use derivedStateOf for computed state
val isScrolledToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex == 0 }
}

// ✅ Use remember with keys
val processedData = remember(rawData) { rawData.process() }

// ✅ Use Immutable/Stable annotations for data classes
@Immutable
public data class TooltipStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val shape: Shape,
)

// ✅ Lambda stability — use remember for lambdas passed down
val onClick = remember(controller) { { controller.next() } }
```

### Accessibility

```kotlin
@Composable
public fun Tooltip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.semantics {
            // Always provide accessibility info
            contentDescription = text
            liveRegion = LiveRegionMode.Polite
        }
    ) {
        // ...
    }
}
```

---

## Documentation Standards

### KDoc Requirements

Every `public` or `protected` declaration MUST have KDoc including:
- Summary line
- `@param` for non-obvious parameters
- `@return` if return type needs explanation
- `@throws` for documented exceptions
- `@since` for version tracking
- `@see` for related APIs
- Code example for complex APIs

```kotlin
/**
 * Displays a tooltip anchored to the given [target] bounds.
 *
 * The tooltip automatically positions itself to avoid going off-screen.
 * If [animated] is true, uses a fade-in animation with the duration
 * specified in [style].
 *
 * ## Example
 *
 * ```kotlin
 * controller.show(
 *     target = buttonBounds,
 *     text = "Click here to save",
 *     position = Position.Top,
 * )
 * ```
 *
 * @param target The bounds of the UI element to anchor to.
 * @param text The tooltip message to display.
 * @param position The preferred position relative to [target]. Defaults to [Position.Bottom].
 * @param animated Whether to animate the appearance. Defaults to `true`.
 * @throws IllegalStateException if the controller has been disposed.
 * @see hide
 * @since 1.0.0
 */
public fun show(
    target: Rect,
    text: String,
    position: Position = Position.Bottom,
    animated: Boolean = true,
)
```

---

## Anti-Patterns Checklist

| Anti-Pattern | Fix |
|---|---|
| `object MySingleton` with mutable state | Use dependency injection, pass instances |
| `lateinit var` in public API | Use constructor injection or nullable with clear docs |
| `String` for IDs, types, colors | Use value classes, sealed types, enums |
| Blocking calls in `suspend` functions | Use `withContext(Dispatchers.IO)` |
| Platform types in common code | Add explicit nullability annotations |
| `Any` or `Object` in public API | Use generics with bounds |
| Swallowing exceptions silently | Log or propagate; use Result for expected failures |
| `GlobalScope.launch` | Accept CoroutineScope from caller |
| Large interfaces (10+ methods) | Split by responsibility (ISP) |
| `companion object` as service locator | Use constructor injection |
