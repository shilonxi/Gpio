package com.example.administrator.gpio;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 该实验侧重外设口及传感器输入输出
 多输入，输入情况相异
 实际按钮控制虚拟按钮
 单输出，LED与按钮对应
 该实验两种按钮使用方法大致相同，具有实用价值
 */

public class Gpio_Activity extends Activity
{
    private static final String TAG="Gpio_Activity";
    private static final String MAGNET_PIN_NAME="BCM2";
    private static final String TOUCH_PIN_NAME="BCM10";
    private static final String LED_PIN_NAME="BCM17";
    //定义端口
    private Switch magnet;
    private Switch touch;
    private ToggleButton led;
    //定义按钮
    private ButtonInputDriver magnet_driver;
    private ButtonInputDriver touch_driver;
    //驱动事件
    private Gpio led_gpio;
    //用于显示
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpio_layout);
        magnet=(Switch)findViewById(R.id.switch3);
        touch=(Switch)findViewById(R.id.switch4);
        led=(ToggleButton)findViewById(R.id.toggleButton);
        //获取实例
        magnet.setChecked(false);
        touch.setChecked(false);
        led.setChecked(false);
        //设置初始状态
        PeripheralManager manager=PeripheralManager.getInstance();
        //连接GPIO端口
        try
        {
            led_gpio=manager.openGpio(LED_PIN_NAME);
            //连接led口
            led_gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            //输出配置为高
        } catch (IOException e) {
            Log.e(TAG,"error on led",e);
        }
        try
        {
            magnet_driver=new ButtonInputDriver(MAGNET_PIN_NAME,Button.LogicState.PRESSED_WHEN_LOW,KeyEvent.KEYCODE_SPACE);
            //低电平视为按钮被按下
            touch_driver=new ButtonInputDriver(TOUCH_PIN_NAME,Button.LogicState.PRESSED_WHEN_HIGH,KeyEvent.KEYCODE_1);
            //高电平视为按钮被按下
            //多输入，输入情况相异
            magnet_driver.register();
            touch_driver.register();
            //注册
        } catch (IOException e) {
            Log.e(TAG,"error on driver",e);
        }
        magnet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton,boolean isChecked)
            {
                if (isChecked)
                {
                    Toast.makeText(Gpio_Activity.this, "magnet open!", Toast.LENGTH_SHORT).show();
                    setValue(true);
                    //灯亮
                }else {
                    Toast.makeText(Gpio_Activity.this, "magnet close!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        touch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton,boolean isChecked)
            {
                if (isChecked)
                {
                    Toast.makeText(Gpio_Activity.this, "touch open!", Toast.LENGTH_SHORT).show();
                    setValue(false);
                    //灯灭
                }else {
                    Toast.makeText(Gpio_Activity.this, "touch close!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //监听
    }
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
        if (keyCode==KeyEvent.KEYCODE_SPACE)
        {
            magnet.setChecked(true);
            //按钮状态切换
            return true;
        }
        else if (keyCode==KeyEvent.KEYCODE_1)
        {
            touch.setChecked(true);
            //按钮状态切换
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    //按钮按下
    @Override
    public boolean onKeyUp(int keyCode,KeyEvent event)
    {
        if (keyCode==KeyEvent.KEYCODE_SPACE)
        {
            magnet.setChecked(false);
            //按钮状态恢复
            return true;
        }
        else if (keyCode==KeyEvent.KEYCODE_1)
        {
            touch.setChecked(false);
            //按钮状态恢复
            return true;
        }
        return super.onKeyUp(keyCode,event);
    }
    //按钮放开
    private void setValue(boolean value)
    {
        try
        {
            led_gpio.setValue(value);
            //控制led
            led.setChecked(value);
            //控制按钮
        } catch (IOException e) {
            Log.e(TAG,"error on value",e);
        }
    }
    //状态显示
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (led_gpio!=null) {
            try {
                led_gpio.close();
            } catch (IOException e) {
                Log.e(TAG,"error on led",e);
            }
        }
        if (magnet_driver!=null) {
            try {
                magnet_driver.unregister();
                //解注册
                magnet_driver.close();
            } catch (IOException e) {
                Log.e(TAG,"error on magnet",e);
            }
        }
        if (touch_driver!=null) {
            try {
                touch_driver.unregister();
                //解注册
                touch_driver.close();
            } catch (IOException e) {
                Log.e(TAG,"error on touch",e);
            }
        }
        //关闭资源
    }
}
