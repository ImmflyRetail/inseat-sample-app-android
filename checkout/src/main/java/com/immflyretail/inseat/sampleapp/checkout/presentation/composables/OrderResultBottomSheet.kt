package com.immflyretail.inseat.sampleapp.checkout.presentation.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.immflyretail.inseat.sampleapp.checkout.R
import com.immflyretail.inseat.sampleapp.ui.AppPersistentBottomSheet
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

@Composable
fun OrderResultBottomSheet(
    isOrderSuccess: Boolean,
    onClickClose: () -> Unit,
    onClickOrderStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppPersistentBottomSheet(
        modifier = modifier,
        imagePainter = if (isOrderSuccess) {
            painterResource(CoreR.drawable.ic_order_success)
        } else {
            painterResource(CoreR.drawable.ic_order_failure)
        },
        title = if (isOrderSuccess) {
            stringResource(R.string.order_result_dialog_title_success)
        } else {
            stringResource(R.string.order_result_dialog_title_failure)
        },
        description = if (isOrderSuccess) {
            stringResource(R.string.order_result_dialog_message_success)
        } else {
            stringResource(R.string.order_result_dialog_message_failure)
        },
        primaryButtonText = if (isOrderSuccess) {
            stringResource(R.string.order_result_dialog_view_order_status_button)
        } else {
            stringResource(R.string.order_result_dialog_try_again_button)
        },
        onPrimaryButtonClick = if (isOrderSuccess) {
            onClickOrderStatus
        } else {
            onClickClose
        },
        secondaryButtonText = if (isOrderSuccess) {
            stringResource(R.string.order_result_dialog_keep_shopping_button)
        } else null,
        onSecondaryButtonClick = if (isOrderSuccess) {
            onClickClose
        } else null,
    )
}
