package comp5216.sydney.edu.au.grocerylist;

import android.content.Context;
import android.text.TextUtils;

public class Filters {

    private String category = "ALL";
    private String storage = "ALL";
    private long from = -30000;
    private long to = 30000;
    private String productName = "ALL";

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();

        return filters;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }
    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (category == null && storage == null && from == -30000 && to == 30000 && productName == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_items));
            desc.append("</b>");
        }

        if (category != null) {
            desc.append("<b>");
            desc.append(category);
            desc.append("</b>");
        }

        if (category != null && storage != null) {
            desc.append(" in ");
        }

        if (storage != null) {
            desc.append("<b>");
            desc.append(storage);
            desc.append("</b>");
        }

        if (from != -30000 && to != 30000) {
            desc.append(" expired days ");
        }

        if (from != -30000) {
            desc.append(" from ");
            desc.append("<b>");
            desc.append(from);
            desc.append("</b>");
        }

        if (to != 30000) {
            desc.append(" to ");
            desc.append("<b>");
            desc.append(to);
            desc.append("</b>");
        }

        if (productName != null) {
            desc.append(" named ");
            desc.append("<b>");
            desc.append(productName);
            desc.append("</b>");
        }

        return desc.toString();
    }

}
