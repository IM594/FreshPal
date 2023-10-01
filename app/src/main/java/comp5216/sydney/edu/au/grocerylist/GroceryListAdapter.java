package comp5216.sydney.edu.au.grocerylist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.ViewHolder> {

    private List<GroceryItem> items;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    // constructor
    public GroceryListAdapter(List<GroceryItem> items) {
        this.items = items;
    }

    // create a new ViewHolder, and inflate the layout item_grocery.xml
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery, parent, false);
        return new ViewHolder(view);
    }

    // bind the data to the view, and set the click listener
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroceryItem item = items.get(position);
        holder.itemNameTextView.setText(item.getGroceryItemName());

        // set the click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position);
            }
        });

        // set the long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemLongClick(position);
            }
            return true; // if don't write this line, the click listener will be triggered twice
        });
    }

    // return the number of items in the list
    @Override
    public int getItemCount() {
        return items.size();
    }

    // viewholder is the container for the view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        // TextView itemDateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initialize the view
            itemNameTextView = itemView.findViewById(R.id.textViewItemName);
            // itemDateTextView = itemView.findViewById(R.id.textViewItemDate);
        }
    }
}
