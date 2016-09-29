package ca.mcgill.ecse321.eventregistration;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import ca.mcgill.ecse321.eventregistration.controller.EventRegistrationController;
import ca.mcgill.ecse321.eventregistration.controller.InvalidInputException;
import ca.mcgill.ecse321.eventregistration.model.Participant;
import ca.mcgill.ecse321.eventregistration.model.RegistrationManager;
import ca.mcgill.ecse321.eventregistration.persistence.PersistenceEventRegistration;


public class MainActivity extends AppCompatActivity {

    public void showDatePickerDialog(View v) {
        TextView tf = (TextView) v;
        Bundle args = getDateFromLabel(tf.getText());
        args.putInt("id", v.getId());
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
    //

    private Bundle getTimeFromLabel(CharSequence text) {
        Bundle rtn = new Bundle();
        String comps[] = text.toString().split(":");
        int hour = 12;
        int minute = 0;
        if (comps.length == 2) {
            hour = Integer.parseInt(comps[0]);
            minute = Integer.parseInt(comps[1]);
        }
        rtn.putInt("hour", hour);
        rtn.putInt("minute", minute);
        return rtn;
    }
    private Bundle getDateFromLabel(CharSequence text) {
        Bundle rtn = new Bundle();
        String comps[] = text.toString().split("-");
        int day = 1;
        int month = 1;
        int year = 1;
        if (comps.length == 3) {
            day = Integer.parseInt(comps[0]);
            month = Integer.parseInt(comps[1]);
            year = Integer.parseInt(comps[2]);
        }
        rtn.putInt("day", day);
        rtn.putInt("month", month-1);
        rtn.putInt("year", year);
        return rtn;
    }

    public void setTime(int id, int h, int m) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(String.format("%02d:%02d", h, m));
    }
    public void setDate(int id, int d, int m, int y) {
        TextView tv = (TextView) findViewById(id);

        tv.setText(String.format("%02d-%02d-%04d", d, m + 1, y));
    }

    private HashMap<Integer, Participant> participants;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PersistenceEventRegistration.setFileName(getFilesDir().getAbsoluteFile().toString());
        PersistenceEventRegistration.loadEventRegistrationModel();




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        refreshData();
    }

    public void refreshData(){
        TextView tv = (TextView) findViewById(R.id.newparticipant_name);
        tv.setText("");
        TextView tv2 = (TextView) findViewById(R.id.newevent_name);
        tv2.setText("");

        //The pdf says to put this code in this method but not sure what this does ...

        // Initialize the data in the participant spinner
        RegistrationManager rm = RegistrationManager.getInstance();
        Spinner spinner = (Spinner) findViewById(R.id.participantspinner);
        ArrayAdapter<CharSequence> participantAdapter = new
                ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        participantAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        this.participants = new HashMap<Integer, Participant>();
        int i = 0;
        for (Iterator<Participant> participants = rm.getParticipants().iterator();
             participants.hasNext(); i++) {
            Participant p = participants.next();
            participantAdapter.add(p.getName());
            this.participants.put(i, p);
        }
        spinner.setAdapter(participantAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addParticipant(View v){
        TextView tv = (TextView) findViewById(R.id.newparticipant_name);
        EventRegistrationController pc = new EventRegistrationController();
        try{
            pc.createParticipant(tv.getText().toString());
            if(tv.getText().toString() == null || tv.getText().toString().trim().length() == 0)
                throw new InvalidInputException("Participant name cannot be empty!");

        } catch (InvalidInputException e){
            //TODO Handle error
            //? not sure what to do here
        }
        refreshData();
    }

    public void addEvent(View v){
        //
        TextView tv = (TextView) findViewById(R.id.newevent_name);
        TextView d = (TextView) findViewById(R.id.newevent_date);
        TextView st = (TextView) findViewById(R.id.newstart_time);
        TextView et = (TextView) findViewById(R.id.newend_time);
        Bundle date = getDateFromLabel(d.getText().toString());
        Bundle start = getTimeFromLabel(st.getText().toString());
        Bundle end = getTimeFromLabel(et.getText().toString());
        Date evDate = new Date(date.getInt("year"),date.getInt("month"),date.getInt("day"));
        Time evStart = new Time(start.getInt("hour"), start.getInt("minute"), 0);
        Time evEnd = new Time(end.getInt("hour"), end.getInt("minute"), 0);
        //DatePickerFragment a = new DatePickerFragment();
        EventRegistrationController pc = new EventRegistrationController();
        try{
            //pc.createParticipant(tv.getText().toString());
            pc.createEvent(tv.getText().toString(),evDate ,evStart, evEnd);

            String error = "";
            if(tv.getText().toString() == null || tv.getText().toString().trim().length() == 0)
                error = error + "Event name cannot be empty! ";
            if(evDate == null)
                error = error + "Event date cannot be empty! ";
            if(evStart == null)
                error = error + "Event start time cannot be empty! ";
            if(evEnd == null)
                error = error + "Event end time cannot be empty! ";
            if(evEnd != null && evStart != null && evEnd.getTime() < evStart.getTime())
                error = error + "Event end time cannot be before event start time! ";
            error = error.trim();
            if(error.length() > 0)
                throw new InvalidInputException(error);

        } catch (InvalidInputException e){
            //Not sure what to do here?
        }
        refreshData();
    }

    public void Register(View v){
        //Something here...
        //I think we need to use spinners
        EventRegistrationController pc = new EventRegistrationController();
        pc.register(Participant ? , Event ?); //gotta register the participant and the event
    }
}
