package android.example.blecentral;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;

public class showListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static class showListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> deviceList;
        private LayoutInflater inflater;

        public showListAdapter(Activity activity) {
            super();
            deviceList = new ArrayList<BluetoothDevice>();
            inflater = activity.getLayoutInflater();
        }

        public void add(BluetoothDevice bluetoothDevice) {
            if (!deviceList.contains(bluetoothDevice)) {
                deviceList.add(bluetoothDevice);
                notifyDataSetChanged();
            }
        }

        public void clear() {
            deviceList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        static class ViewHolder {
            TextView devicename;
            TextView deviceaddress;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.devicename = (TextView) convertView.findViewById(R.id.textview_devicename);
                viewHolder.deviceaddress = (TextView) convertView.findViewById(R.id.textview_deviceaddress);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice bluetoothDevice = deviceList.get(position);
            String Name = bluetoothDevice.getName();
            if (Name != null && 0 < Name.length()) {
                viewHolder.devicename.setText(Name);
            } else {
                viewHolder.devicename.setText("unknown device");
            }
            viewHolder.deviceaddress.setText(bluetoothDevice.getAddress());
            return convertView;
        }
    }

    private String[][] array;
    private String TAG = "DEVICE_INFO";
    private boolean scanning = false;
    private static final long TIME = 10000;

    private showListAdapter showListAdapter;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showlist);

        setResult(Activity.RESULT_CANCELED);
        showListAdapter = new showListAdapter(this);
        ListView listView = (ListView) findViewById(R.id.devicelist);
        listView.setAdapter(showListAdapter);
        listView.setOnItemClickListener(this);

        handler = new Handler();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth isnot supported", LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_showlist, menu);
        if (!scanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.progress).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.progress).setActionView(R.layout.bar_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_scan:
                startScan();
                break;
            case R.id.menu_stop:
                stopScan();
                break;
        }
        return true;
    }

    private ScanCallback scanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showListAdapter.add(result.getDevice());
                    Log.i(TAG, "onScanResult()");
                    Log.i(TAG, "DeviceName:" + result.getDevice().getName());
                    Log.i(TAG, "DeviceAddr:" + result.getDevice().getAddress());
                    Log.i(TAG, "RSSI:" + result.getRssi());
                    Log.i(TAG, "UUID:" + result.getScanRecord().getServiceUuids());
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice bluetoothDevice = (BluetoothDevice) showListAdapter.getItem(position);
        if (bluetoothDevice == null) {
            return;
        }
        Intent intent = new Intent(this, showInfo.class);
        intent.putExtra("NAME", bluetoothDevice.getName());
        intent.putExtra("ADDRESS", bluetoothDevice.getAddress());
        startActivity(intent);
        finish();
    }

    private void startScan() {
        showListAdapter.clear();
        final android.bluetooth.le.BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanning = false;
                scanner.stopScan(scanCallBack);
                Toast.makeText(showListActivity.this, "Stop scan", LENGTH_SHORT).show();
                invalidateOptionsMenu();
            }
        }, TIME);
        scanning = true;
        scanner.startScan(scanCallBack);
        invalidateOptionsMenu();
    }

    private void stopScan() {
        handler.removeCallbacksAndMessages(null);
        android.bluetooth.le.BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) {
            return;
        }
        scanning = false;
        scanner.stopScan(scanCallBack);
        Toast.makeText(showListActivity.this, "Stop scan", LENGTH_SHORT).show();
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }
}
