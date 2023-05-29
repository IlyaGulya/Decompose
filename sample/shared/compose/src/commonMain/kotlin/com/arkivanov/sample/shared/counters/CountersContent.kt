package com.arkivanov.sample.shared.counters

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.sample.shared.counters.counter.CounterComponent
import com.arkivanov.sample.shared.counters.counter.CounterContent
import com.arkivanov.sample.shared.counters.counter.PreviewCounterComponent

@Composable
internal fun CountersContent(component: CountersComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.childStack,
        modifier = modifier,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            animation = stackAnimation { _, _, direction ->
                if (direction.isFront) {
                    slide() + fade()
                } else {
                    scale(frontFactor = 1F, backFactor = 0.7F) + fade()
                }
            },
            onBack = component::onBackClicked,
        ),
    ) {
        CounterContent(
            component = it.instance,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        )
    }
}

@Preview
@Composable
internal fun CountersPreview() {
    CountersContent(component = PreviewCountersComponent())
}

internal class PreviewCountersComponent : CountersComponent {
    override val backHandler: BackHandler = BackDispatcher()

    override val childStack: Value<ChildStack<*, CounterComponent>> =
        MutableValue(
            ChildStack(
                configuration = Unit,
                instance = PreviewCounterComponent(),
            )
        )

    override fun onBackClicked() {}
    override fun onBackClicked(toIndex: Int) {}
}
