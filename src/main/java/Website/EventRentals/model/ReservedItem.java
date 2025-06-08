
package Website.EventRentals.model;

public class ReservedItem {
    private String productId;
    private int count;
    private String name;
    private double price;
    private double deposit;
    private String description;

   public String getProductId() {
    return this.productId;
   }

   public void setProductId(String productId) {
    this.productId = productId;
   }

   public int getCount() {
    return this.count;
   }

   public void setCount(int count) {
    this.count = count;
   }

   public String getName() {
    return this.name;
   }

   public void setName(String name) {
    this.name = name;
   }

   public double getPrice() {
    return this.price;
   }

   public void setPrice(double price) {
    this.price = price;
   }

   public double getDeposit() {
    return this.deposit;
   }

   public void setDeposit(double deposit) {
    this.deposit = deposit;
   }

   public String getDescription() {
    return this.description;
   }

   public void setDescription(String description) {
    this.description = description;
   }
}