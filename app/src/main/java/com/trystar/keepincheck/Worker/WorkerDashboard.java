package com.trystar.keepincheck.Worker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.trystar.keepincheck.Owner.WorkerList;
import com.trystar.keepincheck.R;
import com.trystar.keepincheck.SelectIdentity;
import com.trystar.keepincheck.mapfiles.MapsActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class WorkerDashboard extends AppCompatActivity implements LocationListener {

   // RecyclerView recyclerView;
    ListView listView;
    ArrayList<HashMap<String, Object>> list;
    //ArrayList<Worker> workerArrayList;
    //WorkerAdapter workerAdapter;
    FirebaseFirestore db;
    private LocationManager locationManager;

    String uid;
    FirebaseUser user;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_worker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.ownerprofile:
                Toast.makeText(getApplicationContext(), "View Profile", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, WorkerProfile.class));
                return true;
            case R.id.item2:
                try {
                    startActivity(new Intent(WorkerDashboard.this, WorkerList.class));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.item3:
                Toast.makeText(getApplicationContext(), "Item 3 Selected", Toast.LENGTH_LONG).show();
                startActivity(new Intent(WorkerDashboard.this, JobAssigned.class));
                return true;
            case R.id.item4:
                Toast.makeText(getApplicationContext(), "show location", Toast.LENGTH_LONG).show();
                Intent myIntent = new Intent(WorkerDashboard.this, MapsActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.item5:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(WorkerDashboard.this, SelectIdentity.class));
                Toast.makeText(WorkerDashboard.this, "Signing Out", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();


        LocationHelper helper = new LocationHelper(

                location.getLongitude(), location.getLatitude()
        );
        OnCompleteListener<Void> onCompleteListener;
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getPhoneNumber();
        }
        FirebaseDatabase.getInstance().getReference(uid)
                .setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(WorkerDashboard.this, "Locations Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WorkerDashboard.this, "Location Not Saved", Toast.LENGTH_SHORT).show();
                        }

                    }

                });
    }


    @Override
    public void onProviderDisabled(String provider) {


    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }


    public class LocationHelper {

        private double longitude;
        private double latitude;

        public LocationHelper(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public double getLongitude() {

            return longitude;
        }

        public void setLongitude(double longitude) {

            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }


    }

    String mobile, worker_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        onLocationChanged(lastKnownLocation);

        //recyclerView = findViewById(R.id.recyclerView2);
       // recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listView = findViewById(R.id.taskList);
        list = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mobile = user.getPhoneNumber();
            Toast.makeText(WorkerDashboard.this, mobile, Toast.LENGTH_SHORT).show();
        }

        //workerArrayList = new ArrayList<Worker>();
        // workerAdapter = new WorkerAdapter(WorkerDashboard.this, workerArrayList);

        db.collection("Worker detail")
                .whereEqualTo("Phone Number", mobile)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                worker_name = document.getString("Name");
                                getTaskDetail(worker_name);
                                Toast.makeText(WorkerDashboard.this, "working", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(WorkerDashboard.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        String[] from = {"task", "deadline", "worker"};

        int to[] = {R.id.mcheckbox, R.id.deadline, R.id.worker};
        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), list, R.layout.each_task,from,to);

        listView.setAdapter(simpleAdapter);
        //recyclerView.setAdapter(workerAdapter);

    }

   /* private void EventChangeListener(String worker_name){

        db.collection("task")
                .whereEqualTo("worker", worker_name)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            Toast.makeText(WorkerDashboard.this, "error", Toast.LENGTH_SHORT).show();
                        }
                        assert value != null;
                        for (DocumentChange dc : value.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                workerArrayList.add(dc.getDocument().toObject(Worker.class));
                                Toast.makeText(WorkerDashboard.this,"data added",Toast.LENGTH_SHORT).show();
                            }

                            //workerAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }*/
  private void getTaskDetail(String worker_name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("task")
                .whereEqualTo("worker", worker_name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //task_name.setText(document.getString("task"));
                                //deadline.setText(document.getString("deadline"));
                                //worker.setText(document.getString("worker"));
                                HashMap<String, Object> map = new HashMap<>();

                                // Data entry in HashMap
                                map.put("task", document.getString("task"));
                                map.put("deadline", document.getString("deadline"));
                                map.put("worker", document.getString("worker"));

                                // adding the HashMap to the ArrayList
                                list.add(map);
                                Toast.makeText(WorkerDashboard.this, "working", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(WorkerDashboard.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}


