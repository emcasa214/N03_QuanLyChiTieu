package com.example.n03_quanlychitieu.ui.category;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditCategoryActivity extends DialogFragment {
    CheckBox cbThu, cbChi;
    EditText edtTenDM;
    View colorPreview;
    String id;
    Button btnChonMau, btnChonIcon;

    ImageView iconPreview;


    public static EditCategoryActivity newInstance(String id, String name, String color, String type, String icon) {
        EditCategoryActivity frag = new EditCategoryActivity();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("color", color);
        args.putString("type", type);
        args.putString("icon", icon);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_edit_category, null);

        khoitao(view);
        setCheckBox();
        setMauSac();
        setIcon();

        if (getArguments() != null) {
            id = getArguments().getString("id");
            String name = getArguments().getString("name", ""); // Giá trị mặc định là chuỗi rỗng
            String color = getArguments().getString("color", "#FFFFFF"); // Giá trị mặc định là màu trắng
            String type = getArguments().getString("type", "income"); // Giá trị mặc định là "income"
            String icon = getArguments().getString("icon", ""); // Giá trị mặc định là chuỗi rỗng

            // Nếu ID là null, đây là trường hợp thêm mới
            if (id == null) {
                // Gán giá trị mặc định cho giao diện
                edtTenDM.setText("");
                colorPreview.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF")); // Màu trắng mặc định
                cbThu.setChecked(true); // Mặc định là "income"
                cbChi.setChecked(false);
            } else {
                // Gán giá trị từ Bundle cho giao diện
                edtTenDM.setText(name);
                colorPreview.setBackgroundColor(android.graphics.Color.parseColor(color));
                cbThu.setChecked(type.equals("income"));
                cbChi.setChecked(type.equals("expense"));

                // Gán icon nếu có
                if (!icon.isEmpty()) {
                    int iconResId = getResources().getIdentifier(icon, "drawable", requireContext().getPackageName());
                    if (iconResId != 0) {
                        iconPreview.setImageResource(iconResId);
                        iconPreview.setTag(iconResId); // Gắn tag để lưu ID của icon
                    }
                }
            }
        }

        builder.setView(view)
                .setTitle(id != null ? "Sửa Danh Mục" : "Thêm Danh Mục")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    // Xử lý khi người dùng nhấn nút "Lưu"
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Xử lý khi người dùng nhấn nút "Hủy"
                });
        return builder.create();
    }
    public void khoitao(View view){
        cbThu = view.findViewById(R.id.cbThu);
        cbChi = view.findViewById(R.id.cbChi);
        edtTenDM = view.findViewById(R.id.edtTenDM);
        colorPreview = view.findViewById(R.id.colorPreview);
        btnChonMau = view.findViewById(R.id.btnChonMau);
        btnChonIcon = view.findViewById(R.id.btnChonIcon);
        iconPreview = view.findViewById(R.id.iconPreview);
    }

    public void setCheckBox(){
        // Đảm bảo chỉ chọn được 1 checkbox
        cbThu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbChi.setChecked(false);
            }
        });

        cbChi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbThu.setChecked(false);
            }
        });
    }

    public void setMauSac() {
        // Màu mặc định
        int[] selectedColor = {0xFFF44336}; // Màu đỏ mặc định

        btnChonMau.setOnClickListener(v -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(requireContext(), selectedColor[0], new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    // Cập nhật màu đã chọn
                    selectedColor[0] = color;
                    colorPreview.setBackgroundColor(color); // Hiển thị màu đã chọn
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // Không làm gì nếu người dùng hủy
                }
            });
            colorPicker.show();
        });
    }

    public void setIcon(){
        int[] iconList = {
                R.drawable.icadd,
                R.drawable.icbank,
                R.drawable.icuser,
                R.drawable.icwallet
        };

        btnChonIcon.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
            b.setTitle("Chọn Icon");
            RecyclerView r = new RecyclerView(requireContext());
            r.setLayoutManager(new GridLayoutManager(requireContext(),4));


            RecyclerView.Adapter<IconViewHolder> a = new RecyclerView.Adapter<IconViewHolder>() {
                @NonNull
                @Override
                public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon, parent, false);
                    return new IconViewHolder(view);
                }

                @Override
                public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
                    holder.iconImage.setImageResource(iconList[position]);
                    holder.itemView.setOnClickListener(v1 -> {
                        // Cập nhật icon đã chọn
                        iconPreview.setImageResource(iconList[position]);
                        iconPreview.setTag(iconList[position]);
                    });
                }

                @Override
                public int getItemCount() {
                    return iconList.length;
                }
            };
            r.setAdapter(a);
            b.setView(r);
            AlertDialog dialog = b.create();
            dialog.show();
        });
    }
    class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iconImage);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            posButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = edtTenDM.getText().toString().trim();
                    int colorInt = ((ColorDrawable) colorPreview.getBackground()).getColor();
                    String color = String.format("#%06X", (0xFFFFFF & colorInt));
                    String type = cbThu.isChecked() ? "income" : "expense";
                    String icon = getSelectedIconResourceName();

                    if (name.isEmpty()) {
                        edtTenDM.setError("Tên danh mục không được để trống");
                    } else if (type.isEmpty()) {
                        cbThu.setError("Vui lòng chọn loại danh mục");
                    } else {
                        // Gửi dữ liệu về Activity thông qua OnCategoryUpdatedListener
                        if (getActivity() instanceof OnCategoryUpdatedListener) {
                            ((OnCategoryUpdatedListener) getActivity()).onCategorySaved(id, name, color, type, icon);
                        }
                        dismiss();
                    }
                    dismiss();

                }
            });
        }
    }
    private String getSelectedIconResourceName() {
        if (iconPreview.getDrawable() != null) {
            // Lấy ID của icon đã chọn
            int iconId = (int) iconPreview.getTag();
            // Trả về tên tài nguyên của icon
            return getResources().getResourceEntryName(iconId);
        }
        return ""; // Trả về chuỗi rỗng nếu chưa chọn icon
    }
    public interface OnCategoryUpdatedListener {
        void onCategorySaved(String id, String name, String color, String type, String icon);
    }
}
