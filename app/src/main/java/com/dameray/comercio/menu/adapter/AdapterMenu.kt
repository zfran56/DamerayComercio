package com.dameray.comercio.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dameray.comercio.databinding.CardMenuBinding
import com.dameray.comercio.menu.model.MenuModel

class AdapterMenu (private var items: ArrayList<MenuModel>,
                   private var listener: OnMenuListener,
                   private var context: Context
) :
    RecyclerView.Adapter<AdapterMenu.ViewHolder>() {

    interface OnMenuListener {
        fun onItemClick(position: Int, menu: MenuModel?)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardMenuBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(ArrayList<MenuModel>(items).get(position), listener, context)

    fun replaceData(items: ArrayList<MenuModel>){
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(private var binding: CardMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(menu: MenuModel?, listener: OnMenuListener?, context: Context) {
            binding.menu = menu
            if (listener != null) {
                binding.root.setOnClickListener {
                    listener.onItemClick(layoutPosition, menu)
                }
            }
            binding.executePendingBindings()
        }
    }
}