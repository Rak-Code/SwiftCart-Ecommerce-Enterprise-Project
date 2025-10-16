import random

random.seed(42)  # seed for reproducible output

# Base data
categories = [
    'Clothing', 'Electronics', 'Home & Kitchen', 'Food & Beverage',
    'Fitness', 'Beauty', 'Outdoor', 'Office', 'Decor', 'Accessories'
]
attributes = [
    'comfortable', 'premium', 'organic', 'portable', 'wireless',
    'ergonomic', 'luxury', 'smart', 'durable', 'modern'
]
items = [
    'T-Shirt', 'Jacket', 'Jeans', 'Headphones', 'Speaker', 'Power Bank',
    'Cookware Set', 'Coffee Beans', 'Towel Set', 'Chair', 'Cutting Board',
    'Water Bottle', 'Keyboard', 'Yoga Mat', 'Wine Glass Set', 'Fitness Tracker',
    'Moisturizer', 'Plant', 'Skillet', 'Pillow', 'Desk Lamp', 'Sneakers',
    'Dinnerware', 'Thermostat', 'Tea', 'Dumbbells', 'Candle', 'Tent',
    'French Press', 'Charging Pad', 'Blanket', 'Air Fryer', 'Storage Bags',
    'Kettle', 'Earbuds', 'Laptop Stand', 'Mug Set', 'Backpack', 'Lip Balm', 'LED Bulb'
]

# Price ranges for categories
price_ranges = {
    'Clothing': (25.99, 199.99),
    'Electronics': (49.99, 499.99),
    'Home & Kitchen': (39.99, 299.99),
    'Food & Beverage': (14.99, 79.99),
    'Fitness': (49.99, 299.99),
    'Beauty': (19.99, 129.99),
    'Outdoor': (59.99, 399.99),
    'Office': (39.99, 499.99),
    'Decor': (19.99, 149.99),
    'Accessories': (19.99, 199.99)
}

sizes = ['S', 'M', 'L', 'XL']  # Possible sizes
quantities = range(10, 101)    # Stock quantity range

# Function to generate a random product
def generate_product(used_names):
    category = random.choice(categories)
    item = random.choice(items)
    attribute = random.choice(attributes)
    name = f"{attribute.capitalize()} {item}"

    # Ensure unique names
    attempts = 0
    while name in used_names and attempts < 10:
        attribute = random.choice(attributes)
        name = f"{attribute.capitalize()} {item}"
        attempts += 1
    if name in used_names:
        name = f"{name} {random.randint(100,999)}"

    description = f"{attribute.capitalize()} {item.lower()} for {random.choice(['daily use', 'home use', 'outdoor activities', 'modern lifestyles', 'everyday comfort'])}"
    min_price, max_price = price_ranges[category]
    price = round(random.uniform(min_price, max_price), 2)
    image_url = f"https://picsum.photos/seed/{random.randint(1,10000)}/400/400"
    size = random.choice(sizes)
    quantity = random.choice(quantities)
    category_id = categories.index(category) + 1  # simple category mapping

    return description, image_url, name, price, size, quantity, category_id

# Generate 1000 products
used_names = set()
products = []
while len(products) < 1000:
    product = generate_product(used_names)
    products.append(product)
    used_names.add(product[2])

# Output SQL INSERT statements
print("INSERT INTO products (description, image_url, name, price, size, quantity, category_id) VALUES")
for i, (desc, img, name, price, size, qty, cat_id) in enumerate(products):
    esc_desc = desc.replace("'", "''")
    esc_name = name.replace("'", "''")
    esc_img = img.replace("'", "''")
    end = "," if i < len(products) - 1 else ";"
    print(f"('{esc_desc}', '{esc_img}', '{esc_name}', {price}, '{size}', {qty}, {cat_id}){end}")
