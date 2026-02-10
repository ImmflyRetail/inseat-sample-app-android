package com.immflyretail.inseat.sampleapp.promotion.presentation.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.immflyretail.inseat.sampleapp.promotion.R
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_14
import com.immflyretail.inseat.sdk.api.models.Money
import java.math.BigDecimal
import java.math.RoundingMode
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

@Composable
internal fun SpendLimitProgressBar(
    spendLimit: Money,
    selectedAmount: BigDecimal,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
                .animateContentSize(),
        ) {
            SpendProgressBar(
                selectedAmount = selectedAmount,
                totalAmount = spendLimit.amount
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RemainingAmountLabel(
                    selectedAmount = selectedAmount,
                    totalAmount = spendLimit.amount,
                    currency = spendLimit.currency,
                    modifier = Modifier.weight(1f)
                )

                TotalLimitLabel(
                    amount = spendLimit.amount,
                    currency = spendLimit.currency
                )
            }
        }
    }
}

@Composable
private fun SpendProgressBar(
    selectedAmount: BigDecimal,
    totalAmount: BigDecimal
) {
    val targetProgress = remember(selectedAmount, totalAmount) {
        if (totalAmount > BigDecimal.ZERO) {
            (selectedAmount.divide(totalAmount, 2, RoundingMode.HALF_EVEN))
                .toFloat()
                .coerceIn(0f, 1f)
        } else 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "SpendProgressAnimation"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        drawStopIndicator = {}
    )
}

@Composable
private fun RemainingAmountLabel(
    selectedAmount: BigDecimal,
    totalAmount: BigDecimal,
    currency: String,
    modifier: Modifier = Modifier
) {
    val delta = remember(selectedAmount, totalAmount) {
        totalAmount - selectedAmount
    }

    if (delta > BigDecimal.ZERO) {
        Text(
            text = getRemainingAmountText(delta, currency),
            style = B_14,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier,
        ) {
            Text(
                text = stringResource(R.string.spend_limit_reached),
                style = N_14,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2
            )
            Image(
                painter = painterResource(id = CoreR.drawable.ic_order_success),
                contentDescription = stringResource(id = CoreR.string.completed)
            )
        }
    }
}

@Composable
private fun getRemainingAmountText(
    delta: BigDecimal,
    currency: String
): AnnotatedString = buildAnnotatedString {
    withStyle(style = B_14.toSpanStyle()) {
        append("${delta.toPlainString()}$currency ")
    }
    withStyle(style = N_14.copy(color = MaterialTheme.colorScheme.onSurfaceVariant).toSpanStyle()) {
        append(stringResource(R.string.away_to_unlock_benefits))
    }
}

@Composable
private fun TotalLimitLabel(
    amount: BigDecimal,
    currency: String
) {
    Text(
        text = "${amount.toPlainString()}$currency",
        style = B_14,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        textAlign = TextAlign.End
    )
}