/*file path: app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt*/
/* Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.2
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.askchinna.R
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.ActionCategory
import com.example.askchinna.databinding.ViewActionPlanBinding
import com.example.askchinna.databinding.ItemActionBinding


/**
 * Custom view that displays a list of recommended actions
 * for treating the identified crop issue.
 */
class ActionPlanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val TAG = "ActionPlanView"
    
    // Changed to direct initialization of non-nullable binding
    private val binding: ViewActionPlanBinding = ViewActionPlanBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    
    private val actionAdapter = ActionAdapter()

    init {
        try {
            // Load custom attributes if any
            attrs?.let {
                val typedArray = context.obtainStyledAttributes(it, R.styleable.ActionPlanView)
                try {
                    // Apply attributes
                    val textColor = typedArray.getColor(
                        R.styleable.ActionPlanView_actionTextColor,
                        ContextCompat.getColor(context, R.color.text_primary)
                    )
                    typedArray.getDimensionPixelSize(
                        R.styleable.ActionPlanView_actionIconSize,
                        resources.getDimensionPixelSize(R.dimen.icon_size_medium)
                    )

                    // Direct access to binding properties
                    binding.textActionsTitle.setTextColor(textColor)
                    binding.textNoActions.setTextColor(textColor)
                } finally {
                    typedArray.recycle()
                }
            }

            // Set up RecyclerView
            binding.recyclerActions.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = actionAdapter
                isNestedScrollingEnabled = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ActionPlanView", e)
            throw e
        }
    }

    /**
     * Sets the list of actions to display
     */
    fun setActions(actions: List<Action>) {
        try {
            if (actions.isEmpty()) {
                binding.recyclerActions.visibility = View.GONE
                binding.textNoActions.visibility = View.VISIBLE
            } else {
                binding.recyclerActions.visibility = View.VISIBLE
                binding.textNoActions.visibility = View.GONE
            }
            actionAdapter.submitList(actions)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting actions", e)
            // Show error state
            binding.recyclerActions.visibility = View.GONE
            binding.textNoActions.visibility = View.VISIBLE
            binding.textNoActions.text = context.getString(R.string.error_loading_actions)
        }
    }

    /**
     * Resets the view to its initial state
     */
    fun reset() {
        try {
            binding.recyclerActions.visibility = View.GONE
            binding.textNoActions.visibility = View.VISIBLE
            binding.textNoActions.text = context.getString(R.string.no_actions_available)
            actionAdapter.submitList(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting view", e)
        }
    }

    // No need to set binding to null in onDetachedFromWindow since it's a val

    /**
     * Adapter for displaying action items in a RecyclerView
     */
    private inner class ActionAdapter : RecyclerView.Adapter<ActionAdapter.ActionViewHolder>() {
        private var actions: List<Action> = emptyList()

        fun submitList(newActions: List<Action>) {
            actions = newActions
            this.notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ActionViewHolder {
            val binding = ItemActionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ActionViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
            holder.bind(actions[position])
        }

        override fun getItemCount(): Int = actions.size

        inner class ActionViewHolder(
            private val binding: ItemActionBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(action: Action) {
                binding.apply {
                    textActionTitle.text = action.title
                    textActionDescription.text = action.description

                    // Set action icon based on category
                    val iconResId = when (action.category) {
                        ActionCategory.PEST_CONTROL -> R.drawable.ic_spray
                        ActionCategory.PRUNING -> R.drawable.ic_remove
                        ActionCategory.MONITORING -> R.drawable.ic_monitor
                    }
                    imageActionIcon.setImageResource(iconResId)
                }
            }
        }
    }
}