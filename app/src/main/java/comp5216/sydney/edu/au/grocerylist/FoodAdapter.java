package comp5216.sydney.edu.au.grocerylist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
//自定义Adapter继承自BaseAdapter
public class FoodAdapter extends BaseAdapter {
    private Context context;
    private List<Food> foodList;

    public FoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @Override
    //填充的item的个数
    public int getCount() {
        return foodList.size();
    }

    @Override
    //指定索引对应的item的数据项
    public Object getItem(int position) {
        return foodList.get(position);
    }

    @Override
    //指定索引对应的item的id值
    public long getItemId(int position) {
        return position;
    }

    @Override
    //填充每个item的内容
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = null;
        ViewHolder viewHolder = null;
        if (convertView == null) {
            //加载布局文件，将布局文件转换成View对象
            view = LayoutInflater.from(context).inflate(R.layout.items,null);
            //创建ViewHolder对象
            viewHolder = new ViewHolder();
            //实例化ViewHolder
            viewHolder.foodName = view.findViewById(R.id.item_name);
            viewHolder.storageImage = view.findViewById(R.id.storage_image);
            viewHolder.expiredDays = view.findViewById(R.id.expired_days);
            viewHolder.switchOpened = view.findViewById(R.id.switch_opened);
            //将viewHolder的对象存储到View中
            view.setTag(viewHolder);
        }else{
            view = convertView;
            //取出ViewHolder
            viewHolder = (ViewHolder)view.getTag();
        }
        //给item中各控件赋值
        int expireTime = foodList.get(position).getExpireTime();
        viewHolder.foodName.setText(foodList.get(position).getFoodName());
        viewHolder.expiredDays.setText(String.valueOf(expireTime));
        viewHolder.storageImage.setImageResource(R.drawable.sun);
        viewHolder.switchOpened.setChecked(foodList.get(position).isOpened());
        if (expireTime >= 0 && expireTime <= 1) {
            viewHolder.expiredDays.setTextColor(Color.RED);
        } else if (expireTime > 1 && expireTime <= 3) {
            viewHolder.expiredDays.setTextColor(Color.YELLOW);
        } else if (expireTime > 3) {
            viewHolder.expiredDays.setTextColor(Color.GREEN);
        } else if (expireTime < 0) {
            viewHolder.expiredDays.setTextColor(Color.BLACK);
        }

        return view;
    }
}
//存放item中的所有控件
class ViewHolder{
    TextView foodName;
    ImageView storageImage;
    TextView expiredDays;
    CheckBox switchOpened;
}
