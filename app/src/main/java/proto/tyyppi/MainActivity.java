package proto.tyyppi;

        import android.content.SharedPreferences;
        import android.graphics.Color;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.AutoCompleteTextView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvData, groupV;

    ImageView cogwheelView, writemessageView, sendmessageView, homeView;

    AutoCompleteTextView autoComp;

    Spinner locationSpinner;

    EditText editText;

    String[] Groups;

    String beaconMajor = "123456";
    String groupID = "Ei ryhmää";
    String locationID = "Ei ole";
    String GroupString, LocationString;
    String message = "";

    Button registerBtn, testBtn;

    Boolean rightGroup = false;

    int tabOpen = 2; // 1 = writeMessage, 2 = Home, 3 = Settings.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ff8200"));
            window.setNavigationBarColor(Color.parseColor("#ff8200"));
        }

        cogwheelView = (ImageView) findViewById(R.id.cogwheelID);
        cogwheelView.setImageResource(R.drawable.settings_cogwheel);

        homeView = (ImageView) findViewById(R.id.homeID);
        homeView.setImageResource(R.drawable.home);

        writemessageView = (ImageView) findViewById(R.id.writemessageID);
        writemessageView.setImageResource(R.drawable.write_messagee);

        sendmessageView = (ImageView) findViewById(R.id.sendmessageID);
        sendmessageView.setImageResource(R.drawable.send_message);

        tvData = (TextView) findViewById(R.id.textView);
        groupV = (TextView) findViewById(R.id.groupView);

        editText = (EditText) findViewById(R.id.editText);

        registerBtn = (Button) findViewById(R.id.register);
        registerBtn.setBackgroundColor(Color.parseColor("#ff8200"));
        testBtn = (Button) findViewById(R.id.button);
        testBtn.setBackgroundColor(Color.parseColor("#ff8200"));

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        SharedPreferences sharedPref= getSharedPreferences("mypref", MODE_PRIVATE);
        groupID = sharedPref.getString("savedGroup", groupID);
        locationID = sharedPref.getString("savedLocation", locationID);

        if (groupV == null) {
            groupV.setText("Et ole vielä missään ryhmässä. Valitse ryhmä.");
        } else {
            groupV.setText("Olet ryhmässä: " + groupID + "\nHavaintoasemana: " + locationID + "\n\nVoit vaihtaa näitä asetuksista.");
        }

        // vv Spinner Location valintaan.
        locationSpinner = (Spinner) findViewById(R.id.locationSpinnerView);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.location));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(myAdapter);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocationString = locationSpinner.getSelectedItem().toString();    // Otetaan listasta nimi
                locationID = LocationString;                                      // Asetetaan se

                groupV.setText("Olet ryhmässä: " + groupID + "\nHavaintoasemana: " + locationID + "\n\nVoit vaihtaa näitä asetuksista.");

                int usersChoice = locationSpinner.getSelectedItemPosition();
                SharedPreferences sharedPreff = getSharedPreferences("FileName",0);
                SharedPreferences.Editor prefEditor = sharedPreff.edit();
                prefEditor.putInt("userChoiceSpinner",usersChoice);
                prefEditor.commit();

                saveGroup("savedLocation", locationID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences sharedPreff = getSharedPreferences("FileName",MODE_PRIVATE);
        int spinnerValue = sharedPreff.getInt("userChoiceSpinner",-1);
        if(spinnerValue != -1)
            // set the value of the spinner
            locationSpinner.setSelection(spinnerValue);
        // ^^ Spinner Location valintaan.

        // vv AutoCompleteTextView ryhmän valintaan
        autoComp = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
        Groups = getResources().getStringArray(R.array.groups);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Groups);
        autoComp.setAdapter(adapter);

        autoComp.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

                registerBtn.setVisibility(View.GONE);
                testBtn.setVisibility(View.GONE);

                rightGroup = false;

                if(!autoComp.isPerformingCompletion())
                {
                    boolean validator=false;
                    for(String val :  Groups )
                    {
                        if(val.toLowerCase().startsWith(s.toString().toLowerCase()))
                        {
                            //Toast.makeText(MainActivity.this, "toLowerCase", Toast.LENGTH_SHORT).show();
                            validator=true;
                        }
                    }

                    if(!validator)
                    {
                        Toast.makeText(MainActivity.this, "Oho! Kirjoitusvirhe.", Toast.LENGTH_SHORT).show();
                        autoComp.setText("");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        autoComp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();

                registerBtn.setVisibility(View.VISIBLE);
                rightGroup = true;
            }
        });

        try {
            URL url = new URL("https://oven-sausage.herokuapp.com/add/"+beaconMajor+"/"+groupID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    public void cogwheelClick(View v){
        if (tabOpen == 2) {
            groupV.setVisibility(View.GONE);

            autoComp.setVisibility(View.VISIBLE);
            locationSpinner.setVisibility(View.VISIBLE);
        } else if (tabOpen == 1){
            editText.setVisibility(View.GONE);
            sendmessageView.setVisibility(View.GONE);

            autoComp.setVisibility(View.VISIBLE);
            locationSpinner.setVisibility(View.VISIBLE);
        }
        tabOpen = 3;

        if (rightGroup == true){
            registerBtn.setVisibility(View.VISIBLE);
        }
    }

    public void homeClick(View v){
        if (tabOpen == 3) {
            autoComp.setVisibility(View.GONE);
            locationSpinner.setVisibility(View.GONE);

            testBtn.setVisibility(View.GONE);
            registerBtn.setVisibility(View.GONE);

            groupV.setVisibility(View.VISIBLE);
        } else if (tabOpen == 1){
            editText.setVisibility(View.GONE);
            sendmessageView.setVisibility(View.GONE);

            groupV.setVisibility(View.VISIBLE);
        }
        tabOpen = 2;
    }

    public void writemessageClick(View v){
        if (tabOpen == 2){
            groupV.setVisibility(View.GONE);

            editText.setVisibility(View.VISIBLE);
            sendmessageView.setVisibility(View.VISIBLE);
        } else if (tabOpen == 3){
            autoComp.setVisibility(View.GONE);
            locationSpinner.setVisibility(View.GONE);

            testBtn.setVisibility(View.GONE);
            registerBtn.setVisibility(View.GONE);

            editText.setVisibility(View.VISIBLE);
            sendmessageView.setVisibility(View.VISIBLE);
        }
        tabOpen = 1;
    }

    public void sendmessageClick(View v){
        message = editText.getText().toString();
        message = message.replaceAll(" ", "-");

        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show(); // "Viesti lähetetty."
        //new JSONtask().execute("https://oven-sausage.herokuapp.com/add/"+beaconMajor+"/"+groupID+"/"+locationID+"/"+message);
        editText.setText("");
    }

    public void click(View v) {
        new JSONtask().execute("https://oven-sausage.herokuapp.com/add/"+beaconMajor+"/"+groupID+"/"+locationID);
    }

    public void registerClick(View v){
        GroupString = autoComp.getText().toString();    // Otetaan listasta nimi
        groupID = GroupString;                          // Asetetaan se
        groupV.setText("Olet ryhmässä: " + groupID + "\nHavaintoasemana: " + locationID + "\n\nVoit vaihtaa näitä asetuksista.");    // Tulostetaan se

        testBtn.setVisibility(View.VISIBLE);
        saveGroup("savedGroup", groupID);
    }

    public class JSONtask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = " ";
                while ((line = reader.readLine())!= null){
                    buffer.append(line);
                }
                buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            tvData.setText(result);
        }
    }

    public void saveGroup(String name, String addGroup){
        // Create object of SharedPreferences.
        SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        //now get Editor
        SharedPreferences.Editor editor= sharedPref.edit();
        //put your value
        editor.putString(name, addGroup);
        //commits your edits
        editor.commit();
    }
}