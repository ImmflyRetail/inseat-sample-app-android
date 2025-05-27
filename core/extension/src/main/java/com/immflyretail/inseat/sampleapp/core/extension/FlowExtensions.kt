package com.immflyretail.inseat.sampleapp.core.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Terminal flow operator that launches a new coroutine without blocking the current thread and
 * collects the given flow with a provided [action].
 * [action] runs when [owner] is at least at [minState] and suspends the execution until
 * [owner] is [Lifecycle.State.DESTROYED].
 *
 * @param owner The LifecycleOwner which controls the coroutine.
 * @param minState [Lifecycle.State] in which `block` runs in a new coroutine. That coroutine
 * will cancel if the lifecycle falls below that state, and will restart if it's in that state
 * again.
 * @param action The block to run when the lifecycle is at least in [minState] state.
 */
inline fun <T> Flow<T>.collectWithLifecycle(
    owner: LifecycleOwner,
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minState) {
        this@collectWithLifecycle.collect {
            action(it)
        }
    }
}

/**
 * Extension function that allows an easier call to the API from [Fragment].
 *
 * @see Flow.collectWithLifecycle
 */
inline fun <T> Flow<T>.collectWithLifecycle(
    fragment: Fragment,
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit
) = collectWithLifecycle(fragment.viewLifecycleOwner, minState, action)