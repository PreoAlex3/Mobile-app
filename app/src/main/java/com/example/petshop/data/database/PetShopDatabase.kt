package com.example.petshop.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.petshop.R
import java.util.regex.Pattern

/**
 * Main database class for the PetShop application.
 * Manages all database entities and provides access to DAOs.
 */
@Database(
    entities = [
        Customer::class,
        Category::class,
        Product::class,
        CartItem::class,
        Order::class,
        OrderItem::class
    ],
    version = 20,
    exportSchema = true
)
abstract class PetShopDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao

    companion object {
        private const val DATABASE_NAME = "petshop_database"

        @Volatile
        private var INSTANCE: PetShopDatabase? = null

        fun getInstance(context: Context): PetShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetShopDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PetShopDatabaseCallback(context))
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback for database creation to pre-populate with hardcoded data
     */
    private class PetShopDatabaseCallback(
        private val context: Context
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            CoroutineScope(Dispatchers.IO).launch {
                // Get instance of the database
                val database = getInstance(context)

                // Pre-populate categories
                val categoryList = loadCategories()
                database.categoryDao().insertAllCategories(categoryList)

                // Pre-populate products after categories are inserted
                val productList = loadAllProducts()
                database.productDao().insertAllProducts(productList)
            }
        }


        private fun extractPriceFromResource(resourceId: Int): Double {
            val priceString = context.getString(resourceId)
            val pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)")
            val matcher = pattern.matcher(priceString)
            return if (matcher.find()) {
                matcher.group(1)?.toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }
        }


        private fun loadCategories(): List<Category> {
            return listOf(
                Category(1, "Dog", R.string.Dog, R.drawable.dog),
                Category(2, "Cat", R.string.Cat, R.drawable.cat),
                Category(3, "Bird", R.string.Bird, R.drawable.bird),
                Category(4, "Fish", R.string.Fish, R.drawable.fish),
                Category(5, "Rodent", R.string.Rodent, R.drawable.rodent),
                Category(6, "Reptile", R.string.Reptile, R.drawable.reptile),
                Category(7, "Toy", R.string.Toy, R.drawable.toy),
                Category(9, "Brush", R.string.Brush, R.drawable.brush),
                Category(8, "Leash", R.string.Leash, R.drawable.leash),
            )
        }


        private fun loadAllProducts(): List<Product> {
            val dogFoodList = listOf(
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food1_name,
                    imageResourceId = R.drawable.dog_food1_photo,
                    priceResourceId = R.string.dog_food1_price,
                    price = extractPriceFromResource(R.string.dog_food1_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food1_description
                ),
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food2_name,
                    imageResourceId = R.drawable.dog_food2_photo,
                    priceResourceId = R.string.dog_food2_price,
                    price = extractPriceFromResource(R.string.dog_food2_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food2_description
                ),
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food3_name,
                    imageResourceId = R.drawable.dog_food3_photo,
                    priceResourceId = R.string.dog_food3_price,
                    price = extractPriceFromResource(R.string.dog_food3_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food3_description
                ),
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food4_name,
                    imageResourceId = R.drawable.dog_food4_photo,
                    priceResourceId = R.string.dog_food4_price,
                    price = extractPriceFromResource(R.string.dog_food4_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food4_description
                ),
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food5_name,
                    imageResourceId = R.drawable.dog_food5_photo,
                    priceResourceId = R.string.dog_food5_price,
                    price = extractPriceFromResource(R.string.dog_food5_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food5_description
                ),
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food6_name,
                    imageResourceId = R.drawable.dog_food6_photo,
                    priceResourceId = R.string.dog_food6_price,
                    price = extractPriceFromResource(R.string.dog_food6_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food6_description
                ),
                Product(
                    categoryId = 1, // Dog category
                    nameResourceId = R.string.dog_food7_name,
                    imageResourceId = R.drawable.dog_food7_photo,
                    priceResourceId = R.string.dog_food7_price,
                    price = extractPriceFromResource(R.string.dog_food7_price), // Parse price from resource
                    descriptionResourceId = R.string.dog_food7_description
                )
            )

            val catFoodList = listOf(
                Product(
                    categoryId = 2,
                    nameResourceId = R.string.cat_food1_name,
                    imageResourceId = R.drawable.cat_food1_photo,
                    priceResourceId = R.string.cat_food1_price,
                    price = extractPriceFromResource(R.string.cat_food1_price),
                    descriptionResourceId = R.string.cat_food1_description
                ),
                Product(
                    categoryId = 2,
                    nameResourceId = R.string.cat_food2_name,
                    imageResourceId = R.drawable.cat_food2_photo,
                    priceResourceId = R.string.cat_food2_price,
                    price = extractPriceFromResource(R.string.cat_food2_price),
                    descriptionResourceId = R.string.cat_food2_description
                ),
                Product(
                    categoryId = 2,
                    nameResourceId = R.string.cat_food3_name,
                    imageResourceId = R.drawable.cat_food3_photo,
                    priceResourceId = R.string.cat_food3_price,
                    price = extractPriceFromResource(R.string.cat_food3_price),
                    descriptionResourceId = R.string.cat_food3_description
                ),
                Product(
                    categoryId = 2,
                    nameResourceId = R.string.cat_food4_name,
                    imageResourceId = R.drawable.cat_food4_photo,
                    priceResourceId = R.string.cat_food4_price,
                    price = extractPriceFromResource(R.string.cat_food4_price),
                    descriptionResourceId = R.string.cat_food4_description
                ),
                Product(
                    categoryId = 2,
                    nameResourceId = R.string.cat_food5_name,
                    imageResourceId = R.drawable.cat_food5_photo,
                    priceResourceId = R.string.cat_food5_price,
                    price = extractPriceFromResource(R.string.cat_food5_price),
                    descriptionResourceId = R.string.cat_food5_description
                ),
                Product(
                    categoryId = 2,
                    nameResourceId = R.string.cat_food6_name,
                    imageResourceId = R.drawable.cat_food6_photo,
                    priceResourceId = R.string.cat_food6_price,
                    price = extractPriceFromResource(R.string.cat_food6_price),
                    descriptionResourceId = R.string.cat_food6_description
                )
            )

            val birdFoodList = listOf(
                Product(
                    categoryId = 3,
                    nameResourceId = R.string.bird_food1_name,
                    imageResourceId = R.drawable.bird_food1_photo,
                    priceResourceId = R.string.bird_food1_price,
                    price = extractPriceFromResource(R.string.bird_food1_price),
                    descriptionResourceId = R.string.bird_food1_description
                )
            )

            val fishFoodList = listOf(
                Product(
                    categoryId = 4,
                    nameResourceId = R.string.fish_food1_name,
                    imageResourceId = R.drawable.fish_food1_photo,
                    priceResourceId = R.string.fish_food1_price,
                    price = extractPriceFromResource(R.string.fish_food1_price),
                    descriptionResourceId = R.string.fish_food1_description
                )
            )


            val rodentFoodList = listOf(
                Product(
                    categoryId = 5,
                    nameResourceId = R.string.rodent_food1_name,
                    imageResourceId = R.drawable.rodent_food1_photo,
                    priceResourceId = R.string.rodent_food1_price,
                    price = extractPriceFromResource(R.string.rodent_food1_price),
                    descriptionResourceId = R.string.rodent_food1_description
                )
            )

            val reptileFoodList = listOf(
                Product(
                    categoryId = 6,
                    nameResourceId = R.string.reptile_food1_name,
                    imageResourceId = R.drawable.reptile_food1_photo,
                    priceResourceId = R.string.reptile_food1_price,
                    price = extractPriceFromResource(R.string.reptile_food1_price),
                    descriptionResourceId = R.string.reptile_food1_description
                )
            )

            val toy = listOf(
                Product(
                    categoryId = 7,
                    nameResourceId = R.string.toy_food1_name,
                    imageResourceId = R.drawable.toy_food1_photo,
                    priceResourceId = R.string.toy_food1_price,
                    price = extractPriceFromResource(R.string.toy_food1_price),
                    descriptionResourceId = R.string.toy_food1_description
                )
            )

            val leash = listOf(
                Product(
                    categoryId = 8,
                    nameResourceId = R.string.leash_food1_name,
                    imageResourceId = R.drawable.leash_food1_photo,
                    priceResourceId = R.string.leash_food1_price,
                    price = extractPriceFromResource(R.string.leash_food1_price),
                    descriptionResourceId = R.string.leash_food1_description
                )
            )

            val brush = listOf(
                Product(
                    categoryId = 9,
                    nameResourceId = R.string.brush_food1_name,
                    imageResourceId = R.drawable.brush_food1_photo,
                    priceResourceId = R.string.brush_food1_price,
                    price = extractPriceFromResource(R.string.brush_food1_price),
                    descriptionResourceId = R.string.brush_food1_description
                )
            )

            return dogFoodList + catFoodList + fishFoodList + birdFoodList + rodentFoodList +  reptileFoodList + toy + leash + brush
        }
    }
}