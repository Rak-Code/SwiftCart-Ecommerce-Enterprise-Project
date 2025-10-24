import random

random.seed(42)  # reproducible output

# Base data
categories = [
    'Clothing', 'Electronics', 'Home & Kitchen', 'Food & Beverage',
    'Fitness', 'Beauty', 'Outdoor', 'Office', 'Decor', 'Accessories'
]
attributes = [
    'comfortable', 'premium', 'organic', 'soft', 'warm',
    'lightweight', 'breathable', 'insulated', 'stylish', 'durable'
]

# Focused clothing items only
clothing_items = [
    'T-Shirt', 'Jacket', 'Jeans', 'Sweater', 'Hoodie', 'Shorts',
    'Dress', 'Skirt', 'Sneakers', 'Coat', 'Blazer', 'Tank Top'
]

# Price range for clothing (reuse your mapping)
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

sizes = ['S', 'M', 'L', 'XL']
min_stock, max_stock = 10, 100  # stock quantity range

def generate_clothing_product(used_names):
    category = 'Clothing'
    item = random.choice(clothing_items)
    attribute = random.choice(attributes)
    name = f"{attribute.capitalize()} {item}"

    # Ensure unique names (try a few times, then append random digits)
    attempts = 0
    while name in used_names and attempts < 10:
        attribute = random.choice(attributes)
        name = f"{attribute.capitalize()} {item}"
        attempts += 1
    if name in used_names:
        name = f"{name} {random.randint(100,999)}"

    description = f"{attribute.capitalize()} {item.lower()} for {random.choice(['daily use', 'everyday comfort', 'casual wear', 'outdoor activities', 'work & travel'])}"
    min_price, max_price = price_ranges[category]
    price = round(random.uniform(min_price, max_price), 2)
    image_url = f"/images/{item.lower().replace(' ', '_')}_{random.randint(1,10000)}.jpg"
    size = random.choice(sizes)
    stock_quantity = random.randint(min_stock, max_stock)
    category_id = categories.index(category) + 1  # clothing -> 1

    # Return in the order required by SQL:
    # name, description, price, size, stock_quantity, category_id, image_url
    return name, description, price, size, stock_quantity, category_id, image_url

# Generate N products (1000 like your original)
N = 1000
used_names = set()
products = []
while len(products) < N:
    product = generate_clothing_product(used_names)
    products.append(product)
    used_names.add(product[0])

# Print SQL
print("INSERT INTO products (name, description, price, size, stock_quantity, category_id, image_url) VALUES")
for i, (name, desc, price, size, stock_qty, cat_id, img) in enumerate(products):
    esc_name = name.replace("'", "''")
    esc_desc = desc.replace("'", "''")
    esc_img = img.replace("'", "''")
    end = "," if i < len(products) - 1 else ";"
    print(f"('{esc_name}', '{esc_desc}', {price:.2f}, '{size}', {stock_qty}, {cat_id}, '{esc_img}'){end}")
