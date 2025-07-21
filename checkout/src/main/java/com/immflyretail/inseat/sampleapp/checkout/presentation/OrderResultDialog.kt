package com.immflyretail.inseat.sampleapp.checkout.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.immflyretail.inseat.sampleapp.checkout.R
import com.immflyretail.inseat.sampleapp.ui.InseatButton
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_24_32
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderResultDialog(
    isOrderSuccess: Boolean,
    onClickClose: () -> Unit,
    onClickOrderStatus: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = if (isOrderSuccess) {
                        painterResource(R.drawable.ic_order_success)
                    } else {
                        painterResource(R.drawable.ic_order_failure)
                    },
                    contentDescription = "Order Result",
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = if (isOrderSuccess) {
                        stringResource(R.string.order_result_dialog_title_success)
                    } else {
                        stringResource(R.string.order_result_dialog_title_failure)
                    },
                    style = B_24_32,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isOrderSuccess) {
                        stringResource(R.string.order_result_dialog_message_success)
                    } else {
                        stringResource(R.string.order_result_dialog_message_failure)
                    },
                    style = N_16,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isOrderSuccess) {
                        val redColor = Color(0xFFDD083A)

                        OutlinedButton(
                            onClick = onClickClose,
                            modifier = Modifier.weight(1f).height(48.dp),
                            border = BorderStroke(1.dp, redColor),
                            shape = RoundedCornerShape(size = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.order_result_dialog_keep_shopping_button),
                                style = N_16,
                                color = redColor
                            )
                        }

                        InseatButton(
                            text = stringResource(R.string.order_result_dialog_view_order_status_button),
                            onClick = onClickOrderStatus,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        InseatButton(
                            text = stringResource(R.string.order_result_dialog_try_again_button),
                            onClick = onClickClose,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

    )
}