package nigeriandailies.com.ng.coolgains.buyer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import nigeriandailies.com.ng.coolgains.Prevalent;
import nigeriandailies.com.ng.coolgains.R;
import nigeriandailies.com.ng.coolgains.model.Cart;
import nigeriandailies.com.ng.coolgains.view_holder.CartViewHolder;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private TextView txtTotalAmount, txtMsg1;
    private Button nextPuchaseBtn, TotalBtn;

    private int overalTotalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        checkOrderState();


        txtTotalAmount = findViewById(R.id.display_cart_total_item_price);
        nextPuchaseBtn = findViewById(R.id.next_add_new_item_cart);
        TotalBtn = findViewById(R.id.next_overall_total_new_item_cart);
        txtMsg1 = findViewById(R.id.msg1);


        nextPuchaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTotalAmount.setText( "Total Price = #"+ String.valueOf(overalTotalPrice));

                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(overalTotalPrice));
                startActivity(intent);
                finish();
            }
        });
        TotalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTotalAmount.setText( "Total Price = #"+ String.valueOf(overalTotalPrice));


            }
        });
    }


    @Override
    protected void onStart() {


        super.onStart();


        final DatabaseReference cartList = FirebaseDatabase.getInstance().getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartList.child("Users View")
                        .child(Prevalent.currentOnlineUser.getPhonenumber()).child("Products"), Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                holder.txtProductName.setText(model.getPname());
                holder.txtProdctPrice.setText("Price = "+ "#"+ model.getPrice());
                holder.txtProductQuantity.setText("Quantity = "+ model.getQuantity());

//                the integer.valueOf is use to convert string to integer

                int oneTypeTotalPrice = ((Integer.valueOf(model.getPrice().replace(" ", "")))) *
                        Integer.valueOf(model.getQuantity().replace(" ", ""));
                overalTotalPrice = overalTotalPrice + oneTypeTotalPrice;
//


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{

                                "Edit",
                                "Remove"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                if (i == 0){
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", model.getPid());
                                    startActivity(intent);
                                }
                                if (i == 1){
                                    cartList.child("Users View")
                                            .child(Prevalent.currentOnlineUser.getPhonenumber())
                                            .child("Products")
                                            .child(model.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){
                                                        Toast.makeText(CartActivity.this, "Item removed successfully.",Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }

                                                }
                                            });
                                }

                            }
                        });
                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout,parent,false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    //Method to remove any white space
    public String removeWhitSpace(String text){
        String newText = "";
        while (text.contains(" ")){
            newText = text.replace(" ", "");
        }
        return  newText;
    }
    private  void checkOrderState(){
        DatabaseReference orderRef= FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhonenumber());

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String shippingState = dataSnapshot.child("state").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    if (shippingState.equals("shipped")){
                        txtTotalAmount.setText("Dear" + userName + "\n order is shipped successfully." );
                        recyclerView.setVisibility(View.INVISIBLE);
                        txtMsg1.setVisibility(View.VISIBLE);
                        txtMsg1.setText("congratulations your final_rder has been placed successfully soon it will be verify by our admin");
                        nextPuchaseBtn.setVisibility(View.INVISIBLE);
                        TotalBtn.setVisibility(View.INVISIBLE);

                        Toast.makeText(CartActivity.this, "You can purchase more product, once you received your first final order", Toast.LENGTH_SHORT).show();

                    }else if (shippingState.equals("not shipped")){
                        txtTotalAmount.setText("Shipping state = Not Shipped" );
                        recyclerView.setVisibility(View.INVISIBLE);
                        txtMsg1.setVisibility(View.VISIBLE);
                        nextPuchaseBtn.setVisibility(View.INVISIBLE);
                        TotalBtn.setVisibility(View.INVISIBLE);

                        Toast.makeText(CartActivity.this, "You can purchase more product, once you received your first final order", Toast.LENGTH_SHORT).show();

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

