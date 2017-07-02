package zyzxdev.cryptopal.fragment.dashboard

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_dashboard.*

import zyzxdev.cryptopal.R
import zyzxdev.cryptopal.activity.MainTabbedActivity
import zyzxdev.cryptopal.fragment.dashboard.card.BTCValueCard
import zyzxdev.cryptopal.util.MultiViewAdapter
import zyzxdev.cryptopal.util.DownloadTask
import zyzxdev.cryptopal.util.TaskCompletedCallback

class DashboardFragment : Fragment() {
	val cards = ArrayList<MultiViewAdapter.MultiViewItem>()

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		cards.clear()

		cards.add(BTCValueCard())

		mainListView.adapter = MultiViewAdapter(context, cards)

		//Refresh data now, if necessary
		if(activity is MainTabbedActivity)
			if((activity as MainTabbedActivity).shouldRefresh())
				refreshData()

		swipeRefresh.setOnRefreshListener {
			refreshData(true)
		}
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater!!.inflate(R.layout.fragment_dashboard, container, false)
	}

	private fun refreshData(override: Boolean = false){
		//If we're already refreshing, and !override, return
		if(swipeRefresh!!.isRefreshing && !override) return

		//Set refreshing state to true
		swipeRefresh?.isRefreshing = true

		//Set btcValue to -1 so other activities know that we're refreshing it
		context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putFloat("btcValue", -1f).apply()

		//Download the current 24-hour BTC value, and set it
		DownloadTask(context).setCallback(object: TaskCompletedCallback {
			override fun taskCompleted(data: Object) {
				swipeRefresh?.isRefreshing = false
				if(mainListView != null)
					(mainListView?.adapter as MultiViewAdapter).notifyDataSetChanged()
				try {
					val btc = (data as String).toFloat()
					context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putFloat("btcValue", btc).apply()
				}catch(e: NumberFormatException){
					Toast.makeText(context, R.string.error_updating, Toast.LENGTH_SHORT).show()
				}
			}
		}).execute("https://blockchain.info/q/24hrprice")
	}
}