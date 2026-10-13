<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layoutDirection="rtl"
    android:orientation="vertical"
    android:padding="32dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/app_name"
            android:textColor="@color/accent"
            android:textSize="32sp"
            android:textStyle="bold" />

        <Button
            android:id="@+id/btn_refresh"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:background="@drawable/bg_item"
            android:padding="16dp"
            android:text="@string/refresh"
            android:textColor="@color/text" />

        <Button
            android:id="@+id/btn_settings"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:background="@drawable/bg_item"
            android:padding="16dp"
            android:text="@string/settings"
            android:textColor="@color/text" />
    </LinearLayout>

    <TextView
        android:id="@+id/status"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:textColor="@color/text_dim"
        android:textSize="20sp" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="16dp"
        android:layout_weight="1"
        android:clipToPadding="false" />
</LinearLayout>
