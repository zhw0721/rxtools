package com.demo.xbdz.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    Toolbar toolbar;
    View header;
    View footer;
    int touchSlop = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        touchSlop = (int) (ViewConfiguration.get(MainActivity.this).getScaledTouchSlop() * 0.9);//滚动过多少距离后才开始计算是否隐藏/显示头尾元素。这里用了默认touchslop的0.9倍。
        listView = (ListView) findViewById(R.id.list_view);
        footer = findViewById(R.id.footer);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        // 下面这句将这个ToolBar设置为ActionBar,在这个例子里面，这句其实用不着，但是如果用了这句，就得把Theme设置为NoActionBar了，无关这里要说的，具体见上面的链接中的Style
        //setSupportActionBar(toolbar);

        //为这个ListView填充元素。
        String[] str = new String[64];
        for (int i = 0; i < str.length; i++) {
            str[i] = "Android " + i;
        }
        //R.layout.simple_layout是一个TextView，详见上面的链接……x
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.simple_layout, str);
        listView.setAdapter(adapter);

        //为ListView添加一个Header，这个Header与ToolBar一样高。这样我们可以正确的看到列表中的第一个元素而不被遮住。
        header = new View(MainActivity.this);
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material)));
        header.setBackgroundColor(Color.parseColor("#00000000"));
        listView.addHeaderView(header);

        //为ListView设置触摸事件和滚动事件，这是核心
        listView.setOnTouchListener(onTouchListener);
        listView.setOnScrollListener(onScrollListener);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //为button设置点击事件，点击一次滚动10个item
                listView.smoothScrollByOffset(10);
            }
        });
    }



    AnimatorSet backAnimatorSet;//这是显示头尾元素使用的动画

    private void animateBack() {
        //先清除其他动画
        if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
            hideAnimatorSet.cancel();
        }
        if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
            //如果这个动画已经在运行了，就不管它
        } else {
            backAnimatorSet = new AnimatorSet();
            //下面两句是将头尾元素放回初始位置。
            ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), 0f);
            ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(footer, "translationY", footer.getTranslationY(), 0f);
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(headerAnimator);
            animators.add(footerAnimator);
            backAnimatorSet.setDuration(300);
            backAnimatorSet.playTogether(animators);
            backAnimatorSet.start();
        }
    }

    AnimatorSet hideAnimatorSet;//这是隐藏头尾元素使用的动画

    private void animateHide() {
        //先清除其他动画
        if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
            backAnimatorSet.cancel();
        }
        if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
            //如果这个动画已经在运行了，就不管它
        } else {
            hideAnimatorSet = new AnimatorSet();
            ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(toolbar, "translationY", toolbar.getTranslationY(), -toolbar.getHeight());//将ToolBar隐藏到上面
            ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(footer, "translationY", footer.getTranslationY(), footer.getHeight());//将Button隐藏到下面
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(headerAnimator);
            animators.add(footerAnimator);
            hideAnimatorSet.setDuration(200);
            hideAnimatorSet.playTogether(animators);
            hideAnimatorSet.start();
        }
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {


        float lastY = 0f;
        float currentY = 0f;
        //下面两个表示滑动的方向，大于0表示向下滑动，小于0表示向上滑动，等于0表示未滑动
        int lastDirection = 0;
        int currentDirection = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getY();
                    currentY = event.getY();
                    currentDirection = 0;
                    lastDirection = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (listView.getFirstVisiblePosition() > 0) {
                        //只有在listView.getFirstVisiblePosition()>0的时候才判断是否进行显隐动画。因为listView.getFirstVisiblePosition()==0时，
                        //ToolBar——也就是头部元素必须是可见的，如果这时候隐藏了起来，那么占位置用了headerview就被用户发现了
                        //但是当用户将列表向下拉露出列表的headerview的时候，应该要让头尾元素再次出现才对——这个判断写在了后面onScrollListener里面……
                        float tmpCurrentY = event.getY();
                        if (Math.abs(tmpCurrentY - lastY) > touchSlop) {//滑动距离大于touchslop时才进行判断
                            currentY = tmpCurrentY;
                            currentDirection = (int) (currentY - lastY);
                            if (lastDirection != currentDirection) {
                                //如果与上次方向不同，则执行显/隐动画
                                if (currentDirection < 0) {
                                    animateHide();
                                } else {
                                    animateBack();
                                }
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    //手指抬起的时候要把currentDirection设置为0，这样下次不管向哪拉，都与当前的不同（其实在ACTION_DOWN里写了之后这里就用不着了……）
                    currentDirection = 0;
                    lastDirection = 0;
                    break;
            }
            return false;
        }
    };



    AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {

        //这个Listener其实是用来对付当用户的手离开列表后列表仍然在滑动的情况，也就是SCROLL_STATE_FLING

        int lastPosition = 0;//上次滚动到的第一个可见元素在listview里的位置——firstVisibleItem
        int state = SCROLL_STATE_IDLE;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //记录当前列表状态
            state = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                animateBack();
            }
            if (firstVisibleItem > 0) {
                if (firstVisibleItem > lastPosition && state == SCROLL_STATE_FLING) {
                    //如果上次的位置小于当前位置，那么隐藏头尾元素
                    animateHide();
                }

                //================================
                if (firstVisibleItem < lastPosition && state == SCROLL_STATE_FLING) {
                    //如果上次的位置大于当前位置，那么显示头尾元素，其实本例中，这个if没用
                    //如果是滑动ListView触发的，那么，animateBack()肯定已经执行过了，所以没有必要
                    //如果是点击按钮啥的触发滚动，那么根据设计原则，按钮肯定是头尾元素之一，所以也不需要animateBack()
                    //所以这个if块是不需要的
                    animateBack();
                }
                //这里没有判断(firstVisibleItem == lastPosition && state == SCROLL_STATE_FLING)的情况，
                //但是如果列表中的单个item如果很长的话还是要判断的，只不过代码又要多几行
                //但是可以取巧一下，在触发滑动的时候拖动执行一下animateHide()或者animateBack()——本例中的话就写在那个点击事件里就可以了）
                //BTW，如果列表的滑动纯是靠手滑动列表，而没有类似于点击一个按钮滚到某个位置的话，只要第一个if就够了…

            }
            lastPosition = firstVisibleItem;
        }
    };
}
