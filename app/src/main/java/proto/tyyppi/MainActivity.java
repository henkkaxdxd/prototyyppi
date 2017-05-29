package proto.tyyppi;

        import android.content.SharedPreferences;
        import android.os.AsyncTask;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.AutoCompleteTextView;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvData;
    private TextView groupV;

    AutoCompleteTextView autoComp;
    String[] Groups;

    String beaconMajor = "123456";
    String groupID = "NotSet";
    String GroupString;

    Button registerBtn, testBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvData = (TextView) findViewById(R.id.textView);
        groupV = (TextView) findViewById(R.id.groupView);

        registerBtn = (Button) findViewById(R.id.register);
        testBtn = (Button) findViewById(R.id.button);

        SharedPreferences sharedPref= getSharedPreferences("mypref", MODE_PRIVATE);
        groupID = sharedPref.getString("savedGroup", groupID);

        if (groupV == null) {
            groupV.setText("Et ole vielä missään ryhmässä. Valitse ryhmä.");
        } else {
            groupV.setText("Olet ryhmässä: " + groupID + "\nJos haluat vaihtaa ryhmää syötä uusi ryhmätunnus.");
        }

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
                Toast.makeText(MainActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();

                registerBtn.setVisibility(View.VISIBLE);
            }
        });

        try {
            URL url = new URL("https://oven-sausage.herokuapp.com/add/"+beaconMajor+"/"+groupID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void click(View v) {
        new JSONtask().execute("https://oven-sausage.herokuapp.com/add/"+beaconMajor+"/"+groupID);
    }

    public void registerClick(View v){
        GroupString = autoComp.getText().toString();    // Otetaan listasta nimi
        groupID = GroupString;                          // Asetetaan se
        groupV.setText("Olet ryhmässä: " + groupID + "\nJos haluat vaihtaa ryhmää syötä uusi ryhmätunnus.");    // Tulostetaan se

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