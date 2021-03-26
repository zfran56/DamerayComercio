package com.dameray.rider.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dameray.rider.API
import com.dameray.rider.databinding.CardOrdenesActivasBinding
import com.dameray.rider.menu.model.OrdenesActivasModel

class AdapterDisponibles (private var items: List<OrdenesActivasModel>,
                          private var listener: OnOrdenesActivasListener, private val context: Context
) :
    RecyclerView.Adapter<AdapterDisponibles.ViewHolder>() {

    interface OnOrdenesActivasListener {
        fun onItemClickOrdenesActivas(position: Int, ordenActiva: OrdenesActivasModel?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardOrdenesActivasBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(ArrayList<OrdenesActivasModel>(items).get(position), listener,context)

    fun replaceData(items: ArrayList<OrdenesActivasModel>){
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(private var binding: CardOrdenesActivasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ordenes: OrdenesActivasModel?, listener: OnOrdenesActivasListener?, context: Context) {
            binding.ordensActivas = ordenes
            if(ordenes!!.comercio != null){
                val logo = API.logo + "/" + ordenes.comercio?.get(0)!!.logo
                Glide.with(context).load(logo).into(binding.imgPedido)
            }else{
                val logo = API.logo + "/imagenes/comercios/" + "P4ynXUjan0QUl5sFnn0NeG49finhtzrrtOS7h7rP.png"
                Glide.with(context).load(logo).into(binding.imgPedido)
            }
            if(listener != null){
                binding.buttonDetalle.setOnClickListener {
                    listener.onItemClickOrdenesActivas(layoutPosition, ordenes)
                }
            }
            binding.executePendingBindings()
        }
    }
}