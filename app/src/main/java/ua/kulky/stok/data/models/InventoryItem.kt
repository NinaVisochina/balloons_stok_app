package ua.kulky.stok.data.models

data class InventoryItem(
    val balloonId: Long,
    val code: String,
    val size: String,
    val color: String,
    val price: Double,
    val qtyIn: Int,
    val qtyOut: Int,
    val manufacturer: String
) {
    val stock: Int get() = qtyIn - qtyOut
}
