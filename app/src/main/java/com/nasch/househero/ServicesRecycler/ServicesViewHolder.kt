
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nasch.househero.R

class ServicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvResultado: TextView = itemView.findViewById(R.id.tvServices)

    fun bind(resultado: String) {
        tvResultado.text = resultado
    }
}
