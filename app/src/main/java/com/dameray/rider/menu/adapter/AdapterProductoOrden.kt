package com.dameray.rider.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dameray.rider.databinding.CardProductoOrdenBinding
import com.dameray.rider.menu.model.CarritoModel

class AdapterProductoOrden (private var items: ArrayList<CarritoModel>) :
    RecyclerView.Adapter<AdapterProductoOrden.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardProductoOrdenBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(ArrayList<CarritoModel>(items).get(position))

    fun replaceData(items: ArrayList<CarritoModel>){
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(private var binding: CardProductoOrdenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: CarritoModel?) {
            binding.producto = producto
            binding.executePendingBindings()
        }
    }
}