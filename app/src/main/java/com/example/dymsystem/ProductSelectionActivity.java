package com.example.dymsystem;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.app.Dialog;
import android.widget.Button;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.ImageButton;

public class ProductSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private MaterialButton buttonCart;
    private List<Product> allProducts;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_selection);

        ImageButton buttonBackToMain =
                findViewById(R.id.buttonBackToMain);

        buttonBackToMain.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProductSelectionActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
        });


        recyclerProducts = findViewById(R.id.recyclerProducts);
        buttonCart = findViewById(R.id.buttonCart);
        buttonCart.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProductSelectionActivity.this,
                    CartActivity.class
            );

            startActivity(intent);
        });
        MaterialButton buttonFilter = findViewById(R.id.buttonFilter);

        buttonFilter.setOnClickListener(v -> showFilterDialog());

        // Две колонки
        GridLayoutManager layoutManager =
                new GridLayoutManager(this, 2);

        recyclerProducts.setLayoutManager(layoutManager);

        // Получаем товары из catalog.db
        CatalogRepository repository = new CatalogRepository(this);

        allProducts = repository.getProducts();

        productAdapter = new ProductAdapter(
                this,
                new java.util.ArrayList<>(allProducts),
                (product, quantity, imageView) -> {

                    CartManager.getInstance().addProduct(
                            product,
                            quantity
                    );

                    updateCartButton();

                    animateToCart(imageView);
                }
        );

        recyclerProducts.setAdapter(productAdapter);
        updateCartButton();
    }
    private void updateCartButton() {

        int quantity =
                CartManager.getInstance().getTotalQuantity();

        buttonCart.setText(String.valueOf(quantity));
        buttonCart.setIconResource(R.drawable.ic_shopping_cart);
    }
    private void animateToCart(ImageView sourceImage) {

        ImageView flyingImage = new ImageView(this);

        flyingImage.setImageDrawable(
                sourceImage.getDrawable()
        );

        flyingImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        ViewGroup root = findViewById(android.R.id.content);

        root.addView(
                flyingImage,
                new ViewGroup.LayoutParams(120, 120)
        );

        int[] startLocation = new int[2];
        int[] endLocation = new int[2];

        sourceImage.getLocationOnScreen(startLocation);
        buttonCart.getLocationOnScreen(endLocation);

        float startX = startLocation[0] + 145;
        float startY = startLocation[1] + 110;

        float endX = endLocation[0]
                + buttonCart.getWidth() / 2f
                - 60;

        float endY = endLocation[1]
                + buttonCart.getHeight() / 2f
                - 60;

        flyingImage.setX(startX);
        flyingImage.setY(startY);

        flyingImage.animate()
                .x(endX)
                .y(endY)
                .scaleX(0.2f)
                .scaleY(0.2f)
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {

                    root.removeView(flyingImage);

                })
                .start();
    }
    private void showFilterDialog() {

        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_filter);

        Spinner spinnerFilter1 = dialog.findViewById(R.id.spinnerFilter1);
        Spinner spinnerFilter2 = dialog.findViewById(R.id.spinnerFilter2);
        Spinner spinnerFilter3 = dialog.findViewById(R.id.spinnerFilter3);
        Spinner spinnerFilter4 = dialog.findViewById(R.id.spinnerFilter4);
        Spinner spinnerFilter5 = dialog.findViewById(R.id.spinnerFilter5);
        Spinner spinnerFilter6 = dialog.findViewById(R.id.spinnerFilter6);

        setSpinner(
                spinnerFilter1,
                new String[]{
                        "Все",
                        "Термо",
                        "Одностінний"
                }
        );

        setSpinner(
                spinnerFilter2,
                new String[]{
                        "Все",
                        "304",
                        "321",
                        "201",
                        "430"
                }
        );

        setSpinner(
                spinnerFilter3,
                new String[]{
                        "Все",
                        "0,5 мм",
                        "0,8 мм",
                        "1 мм",
                        "2 мм"
                }
        );

        setSpinner(
                spinnerFilter4,
                new String[]{
                        "Все",
                        "н/н",
                        "н/оц"
                }
        );

        setSpinner(
                spinnerFilter5,
                new String[]{
                        "Все",
                        "100", "110", "120", "125", "130", "140",
                        "150", "160", "170", "180", "190", "200",
                        "210", "220", "230", "240", "250", "260",
                        "270", "280", "290", "300", "310", "320",
                        "330", "350", "360", "370", "380", "400",
                        "420", "450", "460", "500", "520", "860",
                        "100/160", "110/180", "120/180", "130/200",
                        "140/200", "150/220", "160/220", "180/250",
                        "200/260", "220/280", "230/300", "250/320",
                        "300/360", "350/420", "400/460", "500/560",
                        "100/200", "120/220", "130/230", "140/240",
                        "150/250", "160/260", "180/280", "200/300",
                        "100х200", "110х220", "110х230", "110х240",
                        "120х220", "120х230", "120х240"
                }
        );

        setSpinner(
                spinnerFilter6,
                new String[]{
                        "Все",
                        "Труба",
                        "Коліно 45°",
                        "Коліно 90°",
                        "Трійник 90°",
                        "Трійник 45°",
                        "Волпер",
                        "Грибок",
                        "Іскрогасник",
                        "Регулятор тяги(Кагла)",
                        "Лійка",
                        "Окапник",
                        "Закінчення димоходу",
                        "Перехід",
                        "Радіатор",
                        "Ревізія",
                        "Розета",
                        "Сітка",
                        "Скоба",
                        "Криза",
                        "Кронштейн",
                        "Розвантажувальна підставка",
                        "Обжимний хомут",
                        "Хомут під розтяжки",
                        "Стіновий хомут",
                        "Монтажний хомут",
                        "Конус",
                        "Термоґрибок",
                        "Дека",
                        "Заглушка",
                        "Старт-сендвіч",
                        "Труба-подовжувач",
                        "Прохід",
                        "Відображувач",
                        "Труба овальна",
                        "Трійник овальний 90°",
                        "Трійник овальний 45°",
                        "Коліно овальне 45°",
                        "Коліно овальне 90°",
                        "Закінчення димоходу овальне",
                        "Перехід овальний",
                        "Грибок овальний",
                        "Лійка овальна",
                        "Скоба овальна"
                }
        );

        Button btnApplyFilters =
                dialog.findViewById(R.id.btnApplyFilters);

        Button btnResetFilters =
                dialog.findViewById(R.id.btnResetFilters);

        btnApplyFilters.setOnClickListener(v -> {

            applyFilters(
                    spinnerFilter1.getSelectedItem().toString(),
                    spinnerFilter2.getSelectedItem().toString(),
                    spinnerFilter3.getSelectedItem().toString(),
                    spinnerFilter4.getSelectedItem().toString(),
                    spinnerFilter5.getSelectedItem().toString(),
                    spinnerFilter6.getSelectedItem().toString()
            );

            dialog.dismiss();
        });

        btnResetFilters.setOnClickListener(v -> {

            spinnerFilter1.setSelection(0);
            spinnerFilter2.setSelection(0);
            spinnerFilter3.setSelection(0);
            spinnerFilter4.setSelection(0);
            spinnerFilter5.setSelection(0);
            spinnerFilter6.setSelection(0);

            productAdapter.updateList(allProducts);
        });

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            dialog.getWindow().setLayout(
                    (int) (getResources()
                            .getDisplayMetrics()
                            .widthPixels * 0.95),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
    private void applyFilters(
            String chimneyType,
            String grade,
            String thickness,
            String casing,
            String diameter,
            String type
    ) {

        List<Product> filtered = new java.util.ArrayList<>();

        for (Product p : allProducts) {

            boolean ok = true;

            // Тип димоходу
            if (!chimneyType.equals("Все")) {
                ok &= chimneyType.equals(p.getChimneyType());
            }

            // Марка нержавіючої сталі
            if (!grade.equals("Все")) {
                ok &= p.getGrade() == Integer.parseInt(grade);
            }

            // Товщина
            if (!thickness.equals("Все")) {
                ok &= thickness.equals(p.getThickness());
            }

            // Тип оболонки
            if (!casing.equals("Все")) {
                ok &= casing.equals(p.getCasing());
            }

            // Діаметр
            if (!diameter.equals("Все")) {
                ok &= diameter.equals(p.getDiameter());
            }

            // Тип елемента
            if (!type.equals("Все")) {
                ok &= type.equals(p.getType());
            }

            if (ok) {
                filtered.add(p);
            }
        }

        productAdapter.updateList(filtered);
    }
    private void setSpinner(
            Spinner spinner,
            String[] values
    ) {

        android.widget.ArrayAdapter<String> adapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }
}