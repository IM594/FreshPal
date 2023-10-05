package comp5216.sydney.edu.au.grocerylist;

import android.content.Context;
import android.text.TextUtils;

public class Filters {

    private String category = null;
    private String storage = null;
    private String from = null;
    private String to = null;
    private String productName = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();

        return filters;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public boolean hasStorage() {
        return !(TextUtils.isEmpty(storage));
    }

    public boolean hasFrom() {
        return !(TextUtils.isEmpty(from));
    }
    public boolean hasTo() {
        return !(TextUtils.isEmpty(to));
    }

    public boolean hasProductName() {
        return !(TextUtils.isEmpty(productName));
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
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

        if (category == null && storage == null && from == null && to == null && productName == null) {
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

        if (from != null && to != null) {
            desc.append(" expired days ");
        }

        if (from != null) {
            desc.append(" from ");
            desc.append("<b>");
            desc.append(from);
            desc.append("</b>");
        }

        if (to != null) {
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
