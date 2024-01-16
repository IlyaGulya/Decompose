package com.arkivanov.decompose.extensions.compose.stack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.stack.animation.LocalStackAnimationProvider
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.emptyStackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.ChildStackValue
import com.arkivanov.decompose.value.Value

@Composable
fun <C : Any, T : Any> Children(
    stack: ChildStack<C, T>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>? = null,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val holder = rememberSaveableStateHolder()

    holder.retainStates(stack.getConfigurations())

    val animationProvider = LocalStackAnimationProvider.current
    val anim = animation ?: remember(animationProvider, animationProvider::provide) ?: emptyStackAnimation()

    anim(stack = stack, modifier = modifier) { child ->
        holder.SaveableStateProvider(child.configuration.hashString()) {
            content(child)
        }
    }
}

@Composable
fun <C : Any, T : Any> Children(
    stack: Value<ChildStack<C, T>>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>? = null,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val state = stack.subscribeAsState()

    Children(
        stack = state.value,
        modifier = modifier,
        animation = animation,
        content = content
    )
}

@Composable
fun <C : Any, T : Any> Children(
    stack: ChildStackValue<C, T>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>? = null,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val state by stack.subscribeAsState()
    val holder = rememberSaveableStateHolder()

    holder.retainStates(state.getConfigurations())

    val animationProvider = LocalStackAnimationProvider.current
    val anim = animation ?: remember(animationProvider, animationProvider::provide) ?: emptyStackAnimation()
    val visibleConfigurations = remember { HashSet<C>() }

    anim(stack = state, modifier = modifier) { child ->
        holder.SaveableStateProvider(child.configuration.hashString()) {
            content(child)

            DisposableEffect(Unit) {
                visibleConfigurations += child.configuration
                stack.onVisibilityHint(visibleConfigurations)

                onDispose {
                    visibleConfigurations -= child.configuration
                    stack.onVisibilityHint(visibleConfigurations)
                }
            }
        }
    }
}


private fun ChildStack<*, *>.getConfigurations(): Set<String> =
    items.mapTo(HashSet()) { it.configuration.hashString() }

@Composable
private fun SaveableStateHolder.retainStates(currentKeys: Set<Any>) {
    val keys = remember(this) { Keys(currentKeys) }

    DisposableEffect(this, currentKeys) {
        keys.set.forEach {
            if (it !in currentKeys) {
                removeState(it)
            }
        }

        keys.set = currentKeys

        onDispose {}
    }
}

private class Keys(
    var set: Set<Any>
)
