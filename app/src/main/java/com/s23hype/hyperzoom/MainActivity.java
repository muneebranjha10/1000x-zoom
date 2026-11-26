package com.s23hype.hyperzoom;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
public class MainActivity extends Activity {
 int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 @Override public void onCreate(Bundle b){super.onCreate(b);
   LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(24),dp(32),dp(24),dp(24));setContentView(l);
   TextView title=new TextView(this);title.setText("HyperZoom");title.setTextSize(30);title.setTypeface(null,1);l.addView(title);
   TextView label=new TextView(this);label.setText("Maximum zoom");label.setTextSize(16);label.setPadding(0,dp(32),0,0);l.addView(label);
   EditText input=new EditText(this);input.setSingleLine();input.setInputType(InputType.TYPE_CLASS_NUMBER);input.setTextSize(34);input.setGravity(Gravity.CENTER);
   input.setText(String.valueOf(getPreferences(0).getInt("max",1000)));l.addView(input);
   LinearLayout row=new LinearLayout(this);l.addView(row);
   for(int n:new int[]{100,500,1000}){Button p=new Button(this);p.setText(n+"×");p.setOnClickListener(v->input.setText(""+n));row.addView(p,new LinearLayout.LayoutParams(0,dp(56),1));}
   Button open=new Button(this);open.setText("Open test camera");l.addView(open);
   open.setOnClickListener(v->{try{int n=Integer.parseInt(input.getText().toString());if(n<1||n>1000)throw new NumberFormatException();getPreferences(0).edit().putInt("max",n).apply();startActivity(new Intent(this,PhotoActivity.class).putExtra("max",n));}catch(NumberFormatException e){input.setError("1–1000");}});
 }
}
