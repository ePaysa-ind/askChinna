/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.askchinna.R
import com.example.askchinna.data.model.Action
import android.widget.ImageView
import android.widget.TextView

/**
 * Custom view that displays a list of recommended actions for the farmer
 * based on the identification results.
 */
class ActionPlanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val actionsList: RecyclerView
    private val emptyView: TextView
    private val titleView: TextView
    private val actionAdapter: ActionAdapter

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_action_plan, this, true)

        // Initialize views
        actionsList = findViewById(R.id.recycler_actions)
        emptyView = findViewById(R.id.text_no_actions)
        titleView = findViewById(R.id.text_actions_title)

        // Setup RecyclerView
        actionAdapter = ActionAdapter()
        actionsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = actionAdapter
        }
    }

    /**
     * Sets the action plan data to be displayed
     *
     * @param actions List of actions to be displayed in the view
     */
    fun setActions(actions: List<Action>) {
        if (actions.isEmpty()) {
            actionsList.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            actionsList.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            actionAdapter.submitList(actions)
        }
    }

    /**
     * Adapter for the actions RecyclerView
     */
    private inner class ActionAdapter : RecyclerView.Adapter<ActionAdapter.ActionViewHolder>() {

        private var actionsList: List<Action> = emptyList()

        fun submitList(actions: List<Action>) {
            this.actionsList = actions
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_action, parent, false)
            return ActionViewHolder(view)
        }

        override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
            holder.bind(actionsList[position])
        }

        override fun getItemCount() = actionsList.size

        inner class ActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val iconView: ImageView = itemView.findViewById(R.id.image_action_icon)
            private val titleView: TextView = itemView.findViewById(R.id.text_action_title)
            private val descriptionView: TextView = itemView.findViewById(R.id.text_action_description)

            fun bind(action: Action) {
                // Set action icon based on type
                val iconRes = when (action.type) {
                    Action.Type.SPRAY -> R.drawable.ic_spray
                    Action.Type.WATER -> R.drawable.ic_water
                    Action.Type.FERTILIZE -> R.drawable.ic_fertilize
                    Action.Type.REMOVE -> R.drawable.ic_remove
                    Action.Type.MONITOR -> R.drawable.ic_monitor
                }
                iconView.setImageResource(iconRes)

                // Set text content
                titleView.text = action.title
                descriptionView.text = action.description
            }
        }
    }
}