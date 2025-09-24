package com.example.beautyparlor

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.beautyparlor.entities.Service
import com.example.beautyparlor.entities.ServiceSubItem

import com.example.beautyparlor.dao.ServiceDao

class DatabasePrepopulateCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Coroutine to run the prepopulation on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            val appDatabase = AppDatabase.getDatabase(context)
            prepopulateDatabase(appDatabase.serviceDao())
        }
    }

    private suspend fun prepopulateDatabase(serviceDao: ServiceDao) {

        val womenServices = listOf(
            Service(name = "Threading", count = 2, imageResId = R.drawable.haircut, category = "women"),
            Service(name = "Haircut", count = 7, imageResId = R.drawable.haircutimg, category = "women"),
            Service(name = "Hair Straightening", count = 3, imageResId = R.drawable.hairstraight, category = "women"),
            Service(name = "Facial", count = 1, imageResId = R.drawable.facialimg, category = "women"),
            Service(name = "Clean Up", count = 1, imageResId = R.drawable.cleanupimg, category = "women"),
            Service(name = "Bleach", count = 1, imageResId = R.drawable.bleachimg, category = "women"),
            Service(name = "Head Massage", count = 1, imageResId = R.drawable.headmassage, category = "women"),
            Service(name = "Hair Spa", count = 1, imageResId = R.drawable.hairspa, category = "women"),
            Service(name = "Hair Coloring", count = 2, imageResId = R.drawable.haircolor, category = "women"),
            Service(name = "Bridal Hair Brush Makeup", count = 1, imageResId = R.drawable.bridal, category = "women"),
            Service(name = "Basic Makeup", count = 1, imageResId = R.drawable.basic_makeup, category = "women"),
            Service(name = "Normal Makeup", count = 1, imageResId = R.drawable.interimg, category = "women"),


            Service(name = "Ammonia free", count = 3, imageResId = R.drawable.haircolor, category = "women"),

            Service(name = "Keratin Treatment", count = 2, imageResId = R.drawable.kertain, category = "women"),



            Service(name = "Waxing", count = 1, imageResId = R.drawable.waxing, category = "women"),



            Service(name = "Nail Art", count = 2, imageResId = R.drawable.nailart, category = "women"),


            Service(name = "Hair Styling", count = 2, imageResId = R.drawable.hairstyleing, category = "women"),
            Service(name = "Pimple Treatment", count = 1, imageResId = R.drawable.pimple, category = "women"),
            Service(name = "Saree draping", count = 2, imageResId = R.drawable.saree_draping, category = "women")

        )




        val subItems = listOf(
            // Threading
            ServiceSubItem(serviceName = "Threading", subItemName = "Eyebrow", priceRange = "100"),
            ServiceSubItem(serviceName = "Threading", subItemName = "Upper Lip", priceRange = "60 - 70"),

            // Haircut
            ServiceSubItem(serviceName = "Haircut", subItemName = "Straight", priceRange ="300 - 400"),
            ServiceSubItem(serviceName = "Haircut", subItemName = "U Cut", priceRange = "350 - 450"),
            ServiceSubItem(serviceName = "Haircut", subItemName = "Deep U", priceRange = "500 - 650"),
            ServiceSubItem(serviceName = "Haircut", subItemName = "Step cut", priceRange = "800 - 950"),
            ServiceSubItem(serviceName = "Haircut", subItemName = "Layer", priceRange = "900 - 1100"),
            ServiceSubItem(serviceName = "Haircut", subItemName = "Butterfly", priceRange = "1300 - 1800"),
            ServiceSubItem(serviceName = "Haircut", subItemName = "new", priceRange = "1400 - 1800"),

            //Hair Staighting
            ServiceSubItem(serviceName = "Hair Straightening", subItemName = "Temproary", priceRange = "5500 - 6000"),
            ServiceSubItem(serviceName = "Hair Straightening", subItemName = "Permanant", priceRange = "10000 - 11000"),
            ServiceSubItem(serviceName = "Hair Straightening", subItemName = "Ironing", priceRange = "1100 - 2000"),
            // ServiceSubItem(serviceName = "Long Hair Staighting", subItemName = "Fringe Cut", priceRange = "Rs. 300 - 500"),
            // Facial
            ServiceSubItem(serviceName = "Facial", subItemName = "All Types", priceRange = "1000 - 1200 "),
           // Clean Up
            ServiceSubItem(serviceName = "Clean Up", subItemName = "Basic", priceRange = "600 - 700"),

            // Bleach
            ServiceSubItem(serviceName = "Bleach", subItemName = "Face Bleach", priceRange = "400 - 500"),
            // Head Massage
            ServiceSubItem(serviceName = "Head Massage", subItemName = "30-min", priceRange = "800 - 1500"),

            // Hair Spa
            ServiceSubItem(serviceName = "Hair Spa", subItemName = "hair spa", priceRange = "2000 - 4000"),

            // Bridal Services


            ServiceSubItem(serviceName = "Bridal Hair Brush Makeup", subItemName = "Hair brush", priceRange = "30000 - 35000"),

            ServiceSubItem(serviceName = "Basic Makeup", subItemName = "HD", priceRange = "22000 - 25000"),

            ServiceSubItem(serviceName = "Normal Makeup", subItemName = "Make UP", priceRange = "15000 - 18000"),



            // Hair Coloring
            ServiceSubItem(serviceName = "Hair Coloring", subItemName = "Advance", priceRange = "1300 - 1700"),
            ServiceSubItem(serviceName = "Hair Coloring", subItemName = "Root", priceRange = "900 - 1000"),

            // Hair Coloring Ammonia
            ServiceSubItem(serviceName = "Ammonia free", subItemName = "Global", priceRange = "2500 - 3500"),
            ServiceSubItem(serviceName = "Ammonia free", subItemName = "fashion", priceRange = "2800 - 3800"),
            ServiceSubItem(serviceName = "Ammonia free", subItemName = "Highlig", priceRange = "4000 - 4500"),

            ServiceSubItem(serviceName = "Waxing", subItemName = "Wax", priceRange = "750 - 1500"),

            // Keratin Treatment
            ServiceSubItem(serviceName = "Keratin Treatment", subItemName = "basic", priceRange = "1000 - 2000"),
            ServiceSubItem(serviceName = "Keratin Treatment", subItemName = "Global ", priceRange = "2500 - 3500"),


            // Nail Art
            ServiceSubItem(serviceName = "Nail Art", subItemName = "Gel Polish", priceRange = "800 - 1200"),
            ServiceSubItem(serviceName = "Nail Art", subItemName = "Acry", priceRange = "1500 - 3000"),


            // Hair Styling
            ServiceSubItem(serviceName = "Hair Styling", subItemName = "Blow Dry", priceRange = "500 - 800"),

            // Facial Treatment
            ServiceSubItem(serviceName = "Pimple Treatment", subItemName = "Pore", priceRange = "Rs. "),
            ServiceSubItem(serviceName = "Saree draping", subItemName = "Draping", priceRange = "550 - 1000 "),
            ServiceSubItem(serviceName = "Saree draping", subItemName = "Folding", priceRange = " 500 - 1000 "),

        )

        serviceDao.insertServices(womenServices)
        serviceDao.insertSubItems(subItems)
    }
}