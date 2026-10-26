package com.example.tvlauncher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.appGrid)
        rv.layoutManager = GridLayoutManager(this, 5)

        val apps = getInstalledApps()
        val adapter = AppAdapter(apps) { pkg ->
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }
        rv.adapter = adapter
    }

    private fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == packageName) {
                null
            } else {
                AppInfo(
                    resolveInfo.loadLabel(packageManager).toString(),
                    pkg,
                    resolveInfo.loadIcon(packageManager)
                )
            }
        }
    }
}

class AppAdapter(
    private val apps: List<AppInfo>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)

        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            val scale = if (hasFocus) 1.15f else 1.0f
            v.animate().scaleX(scale).scaleY(scale).setDuration(120).start()
            v.elevation = if (hasFocus) 16f else 4f
        }

        holder.itemView.setOnClickListener {
            onClick(app.packageName)
        }
    }

    override fun getItemCount(): Int = apps.size
}
