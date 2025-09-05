package ua.kulky.stock.data.models

data class InventoryItem(
    val balloonId: Long,
    val code: String,
    val size: String,
    val color: String,
    val price: Double,
    val qtyIn: Int,
    val qtyOut: Int
) {
    val stock: Int get() = qtyIn - qtyOut
}
