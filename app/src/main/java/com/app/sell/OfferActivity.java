package com.app.sell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.sell.dao.LoginDao;
import com.app.sell.model.Category;
import com.app.sell.model.Offer;
import com.app.sell.model.User;
import com.app.sell.util.GlideLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.badged_imageview.BadgedImageView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.hdodenhof.circleimageview.CircleImageView;

@EActivity
public class OfferActivity extends AppCompatActivity {

    DatabaseReference databaseOffer;
    DatabaseReference databaseCategory;
    DatabaseReference databaseUser;

    @ViewById(R.id.offer_title)
    TextView offerTitle;
    @ViewById(R.id.offer_description)
    TextView offerDescription;
    @ViewById(R.id.offer_location)
    TextView offerLocation;
    @ViewById(R.id.offer_category)
    TextView categoryName;
    @ViewById(R.id.offer_image)
    BadgedImageView offerImage;
    @ViewById(R.id.offer_user_profile_image)
    CircleImageView offerUserImage;
    @ViewById(R.id.offer_user_name)
    TextView offerUserName;
    @ViewById(R.id.ask)
    Button ask;
    @ViewById(R.id.make_offer)
    Button makeOffer;
    @ViewById(R.id.delete_offer)
    Button deleteOfferBtn;
    @ViewById(R.id.offer_condition)
    TextView offerCondition;
    @ViewById(R.id.offer_fixed_price)
    TextView offerFixedPrice;

    private Offer offer;
    private User user;

    @Bean
    LoginDao loginDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent myIntent = getIntent();
        String id = myIntent.getStringExtra(getString(R.string.field_offer_id));
        databaseOffer = FirebaseDatabase.getInstance().getReference(getString(R.string.db_node_offers)).child(id);
        databaseOffer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                offer = dataSnapshot.getValue(Offer.class);

                offerBinding(offer);

                //user binding
                databaseUser = FirebaseDatabase.getInstance().getReference(getString(R.string.db_node_users)).child(offer.getOffererId());
                databaseUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);
                        offerUserName.setText(user.getFirstName() + " " + user.getLastName());
                        GlideLoader.loadIfValid(OfferActivity.this, user.getImage(), offerUserImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                databaseCategory = FirebaseDatabase.getInstance().getReference(getString(R.string.db_node_categories)).child(offer.getCategoryId());
                databaseCategory.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Category category = dataSnapshot.getValue(Category.class);
                        categoryName.setText(category.getName());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void offerBinding(Offer offer) {

        String city = offer.getLocation().split(",")[2];
        String country = offer.getLocation().split(",")[3];
        String offerPrice = "$" + String.valueOf(offer.getPrice());

        offerTitle.setText(offer.getTitle());
        offerDescription.setText(offer.getDescription());
        offerLocation.setText(city + ", " + country);
        GlideLoader.loadIfValid(this, offer.getImage(), offerImage);
        offerImage.setBadge(offerPrice);
        offerCondition.setText("Condition: " + offer.getCondition());
        offerFixedPrice.setText((offer.getFirmOnPrice() ? "Fixed price!" : "Dynamic price!"));

        if (loginDao.getCurrentUser() != null && loginDao.getCurrentUser().getUid().equals(offer.getOffererId())) {
            ask.setVisibility(View.INVISIBLE);
            makeOffer.setVisibility(View.INVISIBLE);
            if(offer.getIsDeleted()){
                deleteOfferBtn.setVisibility(View.INVISIBLE);
            }
        } else if(!offer.getIsDeleted()){
            deleteOfferBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void openAskActivity(View view) {
        Intent intent = new Intent(view.getContext(), AskActivity_.class);
        intent.putExtra(view.getContext().getString(R.string.field_offerer_id), user.getUid());
        intent.putExtra(view.getContext().getString(R.string.field_offer_id), offer.getId());
        startActivity(intent);
    }

    public void openMakeOfferActivity(View view) {
        Intent intent = new Intent(view.getContext(), MakeOfferActivity_.class);
        intent.putExtra(view.getContext().getString(R.string.field_offerer_id), user.getUid());
        intent.putExtra(view.getContext().getString(R.string.field_offer_id), offer.getId());
        startActivity(intent);
    }

    @Click(R.id.offer_user_area)
    public void openProfileActivity(View view) {
        Intent intent = new Intent(view.getContext(), ProfileActivity_.class);
        intent.putExtra("uid", offer.getOffererId());
        startActivity(intent);
    }

    @Click(R.id.delete_offer)
    public void deleteOffer(View view) {
        FirebaseDatabase.getInstance().getReference(getString(R.string.db_node_offers)).child(offer.getId()).child("isDeleted").setValue(true);
        Intent intent = new Intent(view.getContext(), MainActivity_.class);
        startActivity(intent);
    }
}
