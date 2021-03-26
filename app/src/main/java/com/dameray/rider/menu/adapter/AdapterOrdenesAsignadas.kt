package com.dameray.rider.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dameray.rider.API
import com.dameray.rider.databinding.CardPedidosBinding
import com.dameray.rider.menu.model.OrdenesActivasModel

class AdapterOrdenesAsignadas (private var items: ArrayList<OrdenesActivasModel>,
                               private var listener: OnPedidosListener, private val  context: Context
) :
    RecyclerView.Adapter<AdapterOrdenesAsignadas.ViewHolder>() {

    interface OnPedidosListener {
        fun onItemClickPedidos(position: Int, pedido: OrdenesActivasModel?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardPedidosBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(ArrayList<OrdenesActivasModel>(items).get(position), listener, context)

    fun replaceData(items: ArrayList<OrdenesActivasModel>){
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(private var binding: CardPedidosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pedidos: OrdenesActivasModel?, listener: OnPedidosListener?, context: Context) {
            binding.pedidos = pedidos
            if(pedidos!!.comercio != null){
                val logo = API.logo + "/" + pedidos.comercio?.get(0)!!.logo
                Glide.with(context).load(logo).into(binding.imgPedido)
            }else{
                val logo = API.logo + "/imagenes/comercios/" + "P4ynXUjan0QUl5sFnn0NeG49finhtzrrtOS7h7rP.png"
                Glide.with(context).load(logo).into(binding.imgPedido)
            }
            if (listener != null) {
                binding.buttonDetalle.setOnClickListener {
                    listener.onItemClickPedidos(layoutPosition, pedidos)
                }
            }
            binding.executePendingBindings()
        }
    }
}