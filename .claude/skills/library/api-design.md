# API Design Guide for Kotlin Libraries

## Core Principles

### 1. Minimal Surface Area

Every public declaration is a promise. Less API = less maintenance burden.

```kotlin
// ❌ BAD: Exposing internal machinery
public class TooltipEngine {
    public val renderQueue: MutableList<RenderCommand> = mutableListOf()
    public var currentState: InternalState = InternalState.Idle
    public fun processQueue() { ... }
    public fun updateState(state: InternalState) { ... }
    public fun show(config: TooltipConfig) { ... }
    public fun hide() { ... }
}

// ✅ GOOD: Only what the user needs
public class TooltipController {
    public val isVisible: Boolean get() = ...
    public fun show(config: TooltipConfig) { ... }
    public fun hide() { ... }
}
```

### 2. Discoverable API

Users should find what they need through autocomplete:

```kotlin
// ❌ BAD: Scattered functions
public fun createTooltip(text: String): Tooltip
public fun setTooltipPosition(tooltip: Tooltip, pos: Position)
public fun animateTooltipIn(tooltip: Tooltip)

// ✅ GOOD: Organized through types
public class TooltipController {
    public fun show(text: String, position: Position = Position.Bottom)
    public fun hide(animated: Boolean = true)
}

// ✅ GOOD: Extension functions for optional capabilities
public fun TooltipController.showSequence(steps: List<TooltipStep>)
```

### 3. Hard to Misuse

The compiler should prevent mistakes:

```kotlin
// ❌ BAD: Stringly-typed, easy to mess up
public fun setColor(color: String) // "#FF0000" or "red" or ???
public fun connect(host: String, port: Int) // What if port is negative?

// ✅ GOOD: Type-safe
public fun setColor(color: Color)
public fun connect(address: ServerAddress) // validated in constructor

// ✅ GOOD: Use value classes for type safety without overhead
@JvmInline
public value class Milliseconds(public val value: Long) {
    init { require(value >= 0) { "Duration must be non-negative" } }
}

// ✅ GOOD: Sealed types for finite options
public sealed interface Position {
    public data object Top : Position
    public data object Bottom : Position
    public data class Custom(val x: Float, val y: Float) : Position
}
```

### 4. Sensible Defaults

```kotlin
// ❌ BAD: User must specify everything
public fun showTooltip(
    text: String,
    position: Position,
    animationDuration: Long,
    backgroundColor: Color,
    textColor: Color,
    cornerRadius: Float,
    padding: Float,
    dismissOnTap: Boolean,
    showArrow: Boolean,
)

// ✅ GOOD: Defaults for common cases, config object for customization
public fun showTooltip(
    text: String,
    position: Position = Position.Bottom,
    style: TooltipStyle = TooltipStyle.Default,
)
```

### 5. Consistency

Use the same patterns everywhere:

```kotlin
// If one builder uses this pattern...
public fun tooltip(block: TooltipBuilder.() -> Unit): Tooltip

// ...all builders should use the same pattern
public fun guide(block: GuideBuilder.() -> Unit): Guide
public fun overlay(block: OverlayBuilder.() -> Unit): Overlay
```

---

## Kotlin-Specific API Patterns

### Extension Functions

Use for adding capability to existing types:

```kotlin
// ✅ Natural reading order
"Hello World".toSlug() // reads left-to-right

// ✅ Scoped extensions for context-specific APIs
public class GuideScope {
    public fun Modifier.tooltipTarget(id: String): Modifier
}
```

### Operator Overloading

Use sparingly and only when the semantics are obvious:

```kotlin
// ✅ Clear semantics
public operator fun Offset.plus(other: Offset): Offset
public operator fun Config.plus(override: Config): Config // merging

// ❌ BAD: Non-obvious
public operator fun Tooltip.times(n: Int): List<Tooltip> // ??? 
```

### Sealed Interfaces over Enums

Prefer sealed interfaces when cases might have different data:

```kotlin
// ✅ Flexible, extensible
public sealed interface AnimationSpec {
    public data class Tween(val durationMs: Int = 300) : AnimationSpec
    public data class Spring(val dampingRatio: Float = 0.8f) : AnimationSpec
    public data object None : AnimationSpec
}
```

### Factory Functions

Use instead of constructors when you need flexibility:

```kotlin
// Public factory function - can change implementation later
public fun TooltipController(
    coroutineScope: CoroutineScope,
    config: TooltipConfig = TooltipConfig(),
): TooltipController = TooltipControllerImpl(coroutineScope, config)

// For Compose:
@Composable
public fun rememberTooltipController(
    config: TooltipConfig = TooltipConfig(),
): TooltipController {
    val scope = rememberCoroutineScope()
    return remember { TooltipController(scope, config) }
}
```

### @RequiresOptIn for Experimental APIs

```kotlin
@RequiresOptIn(
    message = "This API is experimental and may change without notice.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalMyLibraryApi

@ExperimentalMyLibraryApi
public fun unstableFeature() { ... }
```

### Deprecation Strategy

```kotlin
@Deprecated(
    message = "Use showTooltip() instead",
    replaceWith = ReplaceWith("showTooltip(text, position)"),
    level = DeprecationLevel.WARNING // -> ERROR in next major -> HIDDEN later
)
public fun show(text: String, position: Position) { ... }
```

---

## Compose API Design

### Composable Function Conventions

```kotlin
// 1. Modifier is always the first optional parameter
@Composable
public fun Tooltip(
    text: String,                              // Required params first
    modifier: Modifier = Modifier,             // Modifier
    position: TooltipPosition = TooltipPosition.Bottom,  // Optional params
    style: TooltipStyle = TooltipStyle.Default,
    onDismiss: (() -> Unit)? = null,           // Callbacks last
) { ... }

// 2. Slot APIs for flexible content
@Composable
public fun Tooltip(
    modifier: Modifier = Modifier,
    style: TooltipStyle = TooltipStyle.Default,
    content: @Composable () -> Unit,           // Trailing lambda for content
) { ... }

// 3. State hoisting pattern
@Composable
public fun TooltipHost(
    state: TooltipState = rememberTooltipState(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) { ... }

@Composable
public fun rememberTooltipState(): TooltipState {
    return remember { TooltipState() }
}

@Stable
public class TooltipState {
    public var isVisible: Boolean by mutableStateOf(false)
        private set
    
    public fun show() { isVisible = true }
    public fun hide() { isVisible = false }
}
```

### Modifier Design

```kotlin
// 1. Chain-friendly — return Modifier
public fun Modifier.tooltipAnchor(
    state: TooltipState,
    id: String,
): Modifier = this.then(TooltipAnchorElement(state, id))

// 2. Compose node approach (modern, preferred)
private class TooltipAnchorElement(
    private val state: TooltipState,
    private val id: String,
) : ModifierNodeElement<TooltipAnchorNode>() {
    override fun create() = TooltipAnchorNode(state, id)
    override fun update(node: TooltipAnchorNode) {
        node.state = state
        node.id = id
    }
    // equals, hashCode ...
}
```

---

## Anti-Patterns

### ❌ God Objects
Don't put everything in one class. Split by responsibility.

### ❌ Returning MutableList/MutableMap
Always return immutable collections from public API.

### ❌ Requiring Initialization
```kotlin
// ❌ BAD
MyLibrary.init(context) // Must call before anything else, or crash

// ✅ GOOD: Lazy initialization or pass context where needed
public fun createClient(context: Context): MyClient
```

### ❌ Callbacks When Coroutines Are Available
```kotlin
// ❌ BAD
public fun loadData(callback: (Result<Data>) -> Unit)

// ✅ GOOD
public suspend fun loadData(): Data
public fun observeData(): Flow<Data>
```

### ❌ Exposing Implementation Types
```kotlin
// ❌ BAD: Leaking Retrofit, OkHttp, or any implementation detail
public fun getHttpClient(): OkHttpClient

// ✅ GOOD: Abstract or hide implementation
internal val httpClient: OkHttpClient = ...
```
