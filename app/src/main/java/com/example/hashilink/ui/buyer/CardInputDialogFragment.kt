package com.example.hashilink.ui.buyer

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hashilink.BuildConfig
import com.example.hashilink.R
import com.stripe.android.view.CardInputWidget

class CardInputDialogFragment : DialogFragment() {

    private var onPaymentConfirmed: ((com.stripe.android.model.PaymentMethodCreateParams) -> Unit)? = null
    private var amount: Double = 0.0
    private var quantity: Int = 1
    private var productName: String = ""

    companion object {
        fun newInstance(
            amount: Double,
            quantity: Int,
            productName: String,
            onPaymentConfirmed: (com.stripe.android.model.PaymentMethodCreateParams) -> Unit
        ): CardInputDialogFragment {
            return CardInputDialogFragment().apply {
                this.amount = amount
                this.quantity = quantity
                this.productName = productName
                this.onPaymentConfirmed = onPaymentConfirmed
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_card_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvPaymentAmount = view.findViewById<TextView>(R.id.tvPaymentAmount)
        val tvPaymentDescription = view.findViewById<TextView>(R.id.tvPaymentDescription)
        val cardInputWidget = view.findViewById<CardInputWidget>(R.id.cardInputWidget)
        val btnPay = view.findViewById<Button>(R.id.btnPay)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val cvTestCardInfo = view.findViewById<androidx.cardview.widget.CardView>(R.id.cvTestCardInfo)

        // Show test card info if in debug mode
        if (BuildConfig.DEBUG) {
            cvTestCardInfo.visibility = View.VISIBLE
        }

        tvPaymentAmount.text = "₹${"%.2f".format(amount)}"
        tvPaymentDescription.text = "$quantity × $productName"

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnPay.setOnClickListener {
            val paymentMethodParams = cardInputWidget.paymentMethodCreateParams

            if (paymentMethodParams == null) {
                Toast.makeText(requireContext(), "Please enter valid card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dismiss()
            onPaymentConfirmed?.invoke(paymentMethodParams)
        }
    }

    override fun onStart() {
        super.onStart()
        // Make dialog full width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
