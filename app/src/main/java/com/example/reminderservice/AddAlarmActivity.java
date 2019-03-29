package com.example.reminderservice;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AddAlarmActivity extends AppCompatActivity {
    private int notificationId = 1;
    DatabaseHelper myDB = new DatabaseHelper(this);
    LinearLayout btnDatePicker, btnTimePicker;
    EditText editText;
    //    Button  cancelBtn ;
    FloatingActionButton setBtn;
    TextView txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    long epoch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String curTime = new SimpleDateFormat("hh:mm a").format(Calendar.getInstance().getTime());
        Log.i(TAG, curTime);

        //get current date time
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);


        setBtn = (FloatingActionButton) findViewById(R.id.setBtn);
//        cancelBtn = (Button)findViewById(R.id.cancelBtn) ;

        btnDatePicker = (LinearLayout) findViewById(R.id.dateLinear);
        btnTimePicker = (LinearLayout) findViewById(R.id.timeLinear);
        editText = findViewById(R.id.editTask);
        txtDate = (TextView) findViewById(R.id.in_date);
        txtDate.setText(date);
        txtTime = (TextView) findViewById(R.id.in_time);
        txtTime.setText(curTime);

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddAlarmActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;

                                txtDate.setText(dateFormat(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year));

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddAlarmActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String am_pm = "";

                                Calendar datetime = Calendar.getInstance();
                                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                datetime.set(Calendar.MINUTE, minute);

                                if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                                    am_pm = "AM";
                                else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                                    am_pm = "PM";

                                String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" : Integer.toString(datetime.get(Calendar.HOUR));

                                mHour = hourOfDay;
                                mMinute = minute;

                                txtTime.setText(timeFormat(strHrsToShow + ":" + datetime.get(Calendar.MINUTE) + " " + am_pm));
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        //set Cancel onclick listener.
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent cancelIntent= new Intent(AddAlarmActivity.this, MainActivity.class);
//                startActivity(cancelIntent);
//            }
//        });

        //set add onclick listener
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = editText.getText().toString();
                String date = dateFormat(txtDate.getText().toString());
//                String date = txtDate.getText().toString();
                String time = timeFormat(txtTime.getText().toString());

                if (message.length() != 0 && time.length() != 0) {

                    String timeStamp = date + " " + time;
                    epoch = timeStampFormat(timeStamp);
                    Log.i(TAG, epoch + "(  " + timeStamp);
                    AddData(message, Long.toString(epoch));
                    editText.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "No Date Time Added !! ", Toast.LENGTH_SHORT).show();
                }

                //set notification & text
                Intent alarmReceiverIntent = new Intent(AddAlarmActivity.this, AlarmReceiver.class);
                alarmReceiverIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                alarmReceiverIntent.putExtra("notificationId", notificationId);
                alarmReceiverIntent.putExtra("todo", message);
                final int _id = (int) System.currentTimeMillis();

                PendingIntent alarmIntent = PendingIntent.getBroadcast(AddAlarmActivity.this, _id, alarmReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);


                //create time
                Calendar startTime = Calendar.getInstance();
                Log.i(TAG, mYear + "----" + mMonth + "---" + mDay + "----" + mHour + "---" + mMinute);
                startTime.set(mYear,mMonth,mDay,mHour,mMinute,0);
                long alarmStartTime = startTime.getTimeInMillis();

                //set alarm.
                alarm.set(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);
                Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
                Intent viewIntent = new Intent(AddAlarmActivity.this, MainActivity.class);
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(viewIntent);
            }
        });

    }

    public void AddData(String msg, String time) {
        boolean inseartData = myDB.addData(msg, time);
        if (inseartData == true) {
            Toast.makeText(getApplicationContext(), "Reminder added Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public String dateFormat(String date) {

        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy");
        Date newDate = null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf = new SimpleDateFormat("dd-MM-yyyy");
        return spf.format(newDate);

    }

    public String timeFormat(String time) {
        SimpleDateFormat spf = new SimpleDateFormat("hh:mm a");
        Date newTime = null;
        try {
            newTime = spf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf = new SimpleDateFormat("hh:mm a");
        return spf.format(newTime);
    }

    public static long timeStampFormat(String timeStamp) {
        long newTimeStamp = 0;
        try {
            newTimeStamp = new SimpleDateFormat("dd-MM-yyyy hh:mm a").parse(timeStamp).getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTimeStamp;
    }

}
