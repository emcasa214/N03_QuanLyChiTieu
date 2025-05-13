package com.example.n03_quanlychitieu.ui.category;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.dao.CategoryDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddCategoryActivity extends AppCompatActivity implements EditCategoryActivity.OnCategoryUpdatedListener {
    private DatabaseHelper db  = new DatabaseHelper(this);
    private RecyclerView.Adapter<MyViewHolder> adapter;
    private RecyclerView recyclerView;
    private CategoryDAO categoryDAO;
    private List<Categories> list;
    private ImageButton btnThem, btnQuayLai;
    RadioGroup radioGroupType;
    private List<Categories> filteredList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        categoryDAO = new CategoryDAO(db.getWritableDatabase());
        khoitao();
//        addSampleData();
        setupRadio();
        recyclerViewHandle();
        btnThemSuaClick();

    }
    public void khoitao(){
        recyclerView = findViewById(R.id.recyclerView);
        btnThem = findViewById(R.id.btnThem);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        radioGroupType = findViewById(R.id.radioGroupType);
    }
    public void setupRadio() {
        // Lắng nghe sự kiện thay đổi trạng thái
        radioGroupType.setOnCheckedChangeListener((group, newCheckedId) -> {
            if (newCheckedId == R.id.radioThu) {
                filterCategories("income"); // Lọc danh mục thu
            } else if (newCheckedId == R.id.radioChi) {
                filterCategories("expense"); // Lọc danh mục chi
            }
        });
    }
    private void filterCategories(String type) {
        filteredList = new ArrayList<>();
        for (Categories c : list) {
            if (c.getType().equals(type)) {
                filteredList.add(c);
            }
        }
        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
    }
    private void addSampleData() {
        SQLiteDatabase database = db.getWritableDatabase();
        database.execSQL("INSERT INTO Categories (category_id, name, icon, color, type) VALUES ('1', 'Danh mục 1', 'icon1', '#FF0000', 'income')");
        database.execSQL("INSERT INTO Categories (category_id, name, icon, color, type) VALUES ('2', 'Danh mục 2', 'icon2', '#00FF00', 'expense')");
    }
    public void btnThemSuaClick(){
        btnThem.setOnClickListener(view -> {
            EditCategoryActivity dialog = EditCategoryActivity.newInstance(null, "","","","");
            dialog.show(getSupportFragmentManager(), "EditCategoryActivity");
        });
        btnQuayLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void recyclerViewHandle(){
        list = categoryDAO.getAllCategories();
        filteredList = new ArrayList<>(list);
        if (list != null && !list.isEmpty()){
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerView.Adapter<MyViewHolder>() {
                @NonNull
                @Override
                public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category,parent,false);
                    return new MyViewHolder(v);
                }

                @Override
                public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                    Categories c = filteredList.get(position);
                    if (c != null){
                        // Lấy icon từ tài nguyên
                        int iconResId = getResources().getIdentifier(c.getIcon(), "drawable", getPackageName());
                        if (iconResId != 0) {
                            // Tạo drawable từ tài nguyên
                            android.graphics.drawable.Drawable iconDrawable = getResources().getDrawable(iconResId, null);
                            if (iconDrawable != null) {
                                // Đặt kích thước cho icon (32px x 32px)
                                int sizeInPx = (int) (32 * getResources().getDisplayMetrics().density); // Chuyển đổi từ dp sang px
                                iconDrawable.setBounds(0, 0, sizeInPx, sizeInPx);
                                holder.txtNguonTien.setCompoundDrawables(iconDrawable, null, null, null); // Đặt icon bên trái
                                holder.txtNguonTien.setCompoundDrawablePadding((int) (8 * getResources().getDisplayMetrics().density));
                            }
                        }

                        // Đổi màu tên thành màu được lưu trong cơ sở dữ liệu
                        holder.txtNguonTien.setTextColor(android.graphics.Color.parseColor(c.getColor()));

                        // Hiển thị tên danh mục
                        holder.txtNguonTien.setText(c.getName());
                        holder.btnSua.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditCategoryActivity dialog = EditCategoryActivity.newInstance(c.getCategory_id(),c.getName(),c.getColor(),c.getType(),c.getIcon());
                                dialog.show(((AppCompatActivity) view.getContext()).getSupportFragmentManager(), "EditCategoryActivity");
                            }
                        });
                        holder.btnXoa.setOnClickListener(view -> {
                            new AlertDialog.Builder(view.getContext())
                                    .setTitle("Xác nhận xóa")
                                    .setMessage("Bạn có chắc chắn muốn xóa danh mục này không?")
                                    .setPositiveButton("Xóa", (dialog, which) -> {
                                        categoryDAO.delete(c.getCategory_id());
                                        list = categoryDAO.getAllCategories(); // Lấy lại danh sách từ cơ sở dữ liệu
                                        filterCategories(radioGroupType.getCheckedRadioButtonId() == R.id.radioThu ? "income" : "expense");
                                        Toast.makeText(view.getContext(), "Xóa danh mục thành công", Toast.LENGTH_SHORT).show();
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });
                    }
                    else{
                        holder.txtNguonTien.setText("Không có dữ liệu");
                    }

                }

                @Override
                public int getItemCount() {
                    return filteredList.size();
                }
            };
            recyclerView.setAdapter(adapter);
        }
        else{
            Toast.makeText(this, "Không có danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategorySaved(String id, String name, String color, String type, String icon) {
        if(id == null){
            Categories c = new Categories(UUID.randomUUID().toString(),name,icon,color,type);
            categoryDAO.insert(c);
        }
        else{
            Categories c = new Categories(id,name,icon,color,type);
            categoryDAO.update(c);
        }
        list = categoryDAO.getAllCategories(); // Lấy lại danh sách từ cơ sở dữ liệu
        adapter.notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txtNguonTien;
        ImageButton btnSua, btnXoa;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNguonTien = itemView.findViewById(R.id.txtNguonTien);
            btnSua = itemView.findViewById(R.id.btnSua);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }


}
