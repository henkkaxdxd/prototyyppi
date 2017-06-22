package proto.tyyppi;

        import android.content.SharedPreferences;
        import android.graphics.Color;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.CountDownTimer;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.view.inputmethod.InputMethodManager;
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

    ImageView cogwheelView, writemessageView, sendmessageView, homeView, beaconlightView, buddylogoView;

    AutoCompleteTextView autoComp;

    Spinner locationSpinner, groupSpinner;

    EditText editText, name, password;

    String[] Groups;

    String bmajori = "123456";
    String groupID = "Ei ryhmää";
    String locationID = "Ei ole";
    String GroupString, LocationString, triedpass, adminName, sendtoGroupString, sendtogroupID;
    String message = "";
    String beaconLight = "off";

    int pass = 22;
    int triedpassint = 0;

    Button registerBtn, adminBtn, adminregisterBtn;

    Boolean rightGroup = false, adminRights = false;

    int tabOpen = 2; // 1 = writeMessage, 2 = Home, 3 = Settings, 4 = Admin login, 5 = Admin login.

    private CountDownTimer timer;

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
        writemessageView.setAlpha(123);

        sendmessageView = (ImageView) findViewById(R.id.sendmessageID);
        sendmessageView.setImageResource(R.drawable.send_message);

        beaconlightView = (ImageView) findViewById(R.id.beaconlightID);
        beaconlightView.setImageResource(R.drawable.beacon_off);

        buddylogoView = (ImageView) findViewById(R.id.buddylogoID);
        buddylogoView.setImageResource(R.drawable.buddy_text);

        tvData = (TextView) findViewById(R.id.textView);
        groupV = (TextView) findViewById(R.id.groupView);

        editText = (EditText) findViewById(R.id.editText);
        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);

        registerBtn = (Button) findViewById(R.id.register);
        registerBtn.setBackgroundColor(Color.parseColor("#ff8200"));
        adminBtn = (Button) findViewById(R.id.admin);
        adminBtn.setBackgroundColor(Color.parseColor("#ff8200"));
        adminregisterBtn = (Button) findViewById(R.id.adminregister);
        adminregisterBtn.setBackgroundColor(Color.parseColor("#ff8200"));

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        SharedPreferences sharedPref= getSharedPreferences("mypref", MODE_PRIVATE);
        groupID = sharedPref.getString("savedGroup", groupID);
        locationID = sharedPref.getString("savedLocation", locationID);
        beaconLight = sharedPref.getString("savedBeacon", beaconLight);

        if (groupV == null) {
            groupV.setText("Et ole vielä missään ryhmässä. Valitse ryhmä.");
        } else {
            groupV.setText("Olet ryhmässä: " + groupID + "\nHavaintoasemana: " + locationID + "\n\nVoit vaihtaa näitä asetuksista.");
        }

        findViewById(R.id.cLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });

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

                saveStuff("savedLocation", locationID);

                if (!autoComp.getText().toString().isEmpty()){
                    registerBtn.setVisibility(View.VISIBLE);
                }
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

        // vv Spinner Group valintaan (viestitys).
        groupSpinner = (Spinner) findViewById(R.id.groupSpinnerView);

        ArrayAdapter<String> myAdapter2 = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sendtogroup));
        myAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(myAdapter2);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendtoGroupString = groupSpinner.getSelectedItem().toString();    // Otetaan listasta nimi
                sendtogroupID = sendtoGroupString;

                Toast.makeText(MainActivity.this, sendtogroupID, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // ^^ Spinner Group valintaan (viestitys).

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
            URL url = new URL("https://oven-sausage.herokuapp.com/add/1/"+bmajori+"/"+groupID+"/"+locationID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        timer = new CountDownTimer(2000, 20) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                try{
                    yourMethod();
                }catch(Exception e){
                    Log.e("Error", "Error: " + e.toString());
                }
            }
        }.start();
    }

    public void yourMethod(){
        SharedPreferences sharedPref = getSharedPreferences("mypref",MODE_PRIVATE);
        beaconLight = sharedPref.getString("savedBeacon", beaconLight);

        if (beaconLight.equals("on")){
            beaconlightView.setImageResource(R.drawable.beacon_on);
        } else if (beaconLight.equals("off")){
            beaconlightView.setImageResource(R.drawable.beacon_off);
        }
        timer.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    public void cogwheelClick(View v){
        settings();
    }

    public void settings(){
        if (tabOpen == 2) {
            groupV.setVisibility(View.GONE);
            buddylogoView.setVisibility(View.GONE);

            adminBtn.setVisibility(View.VISIBLE);
            autoComp.setVisibility(View.VISIBLE);
            locationSpinner.setVisibility(View.VISIBLE);
        } else if (tabOpen == 1){
            editText.setVisibility(View.GONE);
            sendmessageView.setVisibility(View.GONE);
            groupSpinner.setVisibility(View.GONE);

            adminBtn.setVisibility(View.VISIBLE);
            autoComp.setVisibility(View.VISIBLE);
            locationSpinner.setVisibility(View.VISIBLE);
        } else if (tabOpen > 3){
            name.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            adminregisterBtn.setVisibility(View.GONE);

            adminBtn.setVisibility(View.VISIBLE);
            autoComp.setVisibility(View.VISIBLE);
            locationSpinner.setVisibility(View.VISIBLE);
        } else if (tabOpen == 3){
            adminBtn.setVisibility(View.VISIBLE);
            autoComp.setVisibility(View.VISIBLE);
            locationSpinner.setVisibility(View.VISIBLE);
        }
        tabOpen = 3;

        if (rightGroup == true){
            registerBtn.setVisibility(View.VISIBLE);
        }

        if (adminRights == false){
            adminBtn.setText("Hae admin oikeudet");
        } else if (adminRights == true){
            adminBtn.setText("Poista admin oikeudet");
        }
    }

    public void homeClick(View v){
        if (tabOpen == 3) {
            autoComp.setVisibility(View.GONE);
            locationSpinner.setVisibility(View.GONE);

            registerBtn.setVisibility(View.GONE);
            adminBtn.setVisibility(View.GONE);

            groupV.setVisibility(View.VISIBLE);
            buddylogoView.setVisibility(View.VISIBLE);
        } else if (tabOpen == 1){
            editText.setVisibility(View.GONE);
            sendmessageView.setVisibility(View.GONE);
            groupSpinner.setVisibility(View.GONE);

            groupV.setVisibility(View.VISIBLE);
            buddylogoView.setVisibility(View.VISIBLE);
        } else if (tabOpen > 3){
            name.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            adminregisterBtn.setVisibility(View.GONE);

            groupV.setVisibility(View.VISIBLE);
            buddylogoView.setVisibility(View.VISIBLE);
        }
        tabOpen = 2;
    }

    public void writemessageClick(View v){
        if (adminRights == true) {
            if (tabOpen == 2) {
                groupV.setVisibility(View.GONE);
                buddylogoView.setVisibility(View.GONE);

                editText.setVisibility(View.VISIBLE);
                sendmessageView.setVisibility(View.VISIBLE);
                groupSpinner.setVisibility(View.VISIBLE);
            } else if (tabOpen == 3) {
                autoComp.setVisibility(View.GONE);
                locationSpinner.setVisibility(View.GONE);

                registerBtn.setVisibility(View.GONE);
                adminBtn.setVisibility(View.GONE);

                editText.setVisibility(View.VISIBLE);
                sendmessageView.setVisibility(View.VISIBLE);
                groupSpinner.setVisibility(View.VISIBLE);
            } else if (tabOpen > 3) {
                name.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                adminregisterBtn.setVisibility(View.GONE);

                editText.setVisibility(View.VISIBLE);
                sendmessageView.setVisibility(View.VISIBLE);
                groupSpinner.setVisibility(View.VISIBLE);
            }
            tabOpen = 1;
        } else if (adminRights == false){
            Toast.makeText(MainActivity.this, "Viestitys vaatii admin oikeudet.", Toast.LENGTH_SHORT).show();
        }
    }

    public void adminClick(View v){
        tabOpen = 4;
        if (adminRights == false) {
            autoComp.setVisibility(View.GONE);
            locationSpinner.setVisibility(View.GONE);
            adminBtn.setVisibility(View.GONE);
            registerBtn.setVisibility(View.GONE);

            name.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            adminregisterBtn.setVisibility(View.VISIBLE);
        } else if (adminRights == true){
            adminRights = false;
            Toast.makeText(MainActivity.this, "Admin oikeudet poistettu.", Toast.LENGTH_SHORT).show();
            adminBtn.setText("Hae admin oikeudet");
            writemessageView.setAlpha(123);

            tabOpen = 3;
            settings();
        }
    }

    public void adminregisterClick(View v){
        tabOpen = 5;
        if (name.getText().toString().equals("")){
            Toast.makeText(MainActivity.this, "Syötä koko nimesi.", Toast.LENGTH_SHORT).show();
        } else if (password.getText().toString().equals("")){
            Toast.makeText(MainActivity.this, "Syötä salasana.", Toast.LENGTH_SHORT).show();
        } else {
            adminName = name.getText().toString();
            triedpass = password.getText().toString();
            if (android.text.TextUtils.isDigitsOnly(triedpass) == true){    // testaa että salasanassa on vain numeroita
                triedpassint = Integer.valueOf(triedpass);
                if (triedpassint == pass) {
                    Toast.makeText(MainActivity.this, "Oikea salasana! Admin oikeudet annettu.", Toast.LENGTH_SHORT).show();
                    writemessageView.setAlpha(255);
                    adminRights = true;

                    name.setVisibility(View.GONE);
                    password.setVisibility(View.GONE);
                    adminregisterBtn.setVisibility(View.GONE);

                    tabOpen = 3;
                    settings();
                } else if (triedpassint != pass) {
                    Toast.makeText(MainActivity.this, "Väärä salasana!", Toast.LENGTH_SHORT).show();
                    password.setText("");
                }
            } else {
                Toast.makeText(MainActivity.this, "Väärä salasana!", Toast.LENGTH_SHORT).show();
                password.setText("");
            }
        }
    }

    public void sendmessageClick(View v){
        SharedPreferences sharedPref = getSharedPreferences("mypref",MODE_PRIVATE);
        bmajori = sharedPref.getString("major", bmajori);
        message = editText.getText().toString();
        message = message.replaceAll(" ", "-");
        message = message.replaceAll("/", " tai ");
        adminName = adminName.replaceAll(" ", "-");

        if (message.isEmpty()){
            Toast.makeText(MainActivity.this, "Viesti kenttäsi on tyhjä!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, sendtogroupID+"/"+message+"/"+adminName, Toast.LENGTH_LONG).show(); // "Viesti lähetetty."
            new JSONtask().execute("https://oven-sausage.herokuapp.com/viesti/"+sendtogroupID+"/"+message+"/"+adminName); //lisää tähän lähettäjän nimi (adminName)?
            editText.setText("");
        }
    }

    public void refresh(){
        // Eli jos olet beaconin alueella ja vaihdat asetuksia, näytölle päivitetään tiedot sinun uusien asetuksien mukaan tämän avulla.
        SharedPreferences sharedPref = getSharedPreferences("mypref",MODE_PRIVATE);
        beaconLight = sharedPref.getString("savedBeacon", beaconLight);

        if (beaconLight.equals("on")){
            new JSONtask().execute("https://oven-sausage.herokuapp.com/add/1/"+bmajori+"/"+groupID+"/"+locationID);
        }
    }

    /* ------------ TESTAA TÄMÄ ------------- */
    /*
    public static void hideKeyboard(MainActivity activity) {
        if (activity != null) {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(MainActivity
                        .INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }
    }
    */

    public void registerClick(View v){
        GroupString = autoComp.getText().toString();    // Otetaan listasta nimi
        groupID = GroupString;                          // Asetetaan se
        groupV.setText("Olet ryhmässä: " + groupID + "\nHavaintoasemana: " + locationID + "\n\nVoit vaihtaa näitä asetuksista.");    // Tulostetaan se

        Toast.makeText(MainActivity.this, "Uudet tiedot asetettu.", Toast.LENGTH_SHORT).show();

        registerBtn.setVisibility(View.GONE);

        saveStuff("savedGroup", groupID);
        refresh();
    }

    public void beaconClick(View v){
        SharedPreferences sharedPref = getSharedPreferences("mypref",MODE_PRIVATE);
        bmajori = sharedPref.getString("major", bmajori);

        Toast.makeText(MainActivity.this, bmajori, Toast.LENGTH_SHORT).show();
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

    public void saveStuff(String name, String addGroup){
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