import sys

# Check if at least one log file is provided
if len(sys.argv) < 2:
    print("Usage: python log_processing.py <log_file1> [<log_file2> ...]")
    sys.exit(1)

# Initialize variables
total_col1 = 0
total_col2 = 0
num_lines = 0

# Process each log file
for i in range(1, len(sys.argv)):
    log_file = sys.argv[i]

    # Read the log file
    with open(log_file, 'r') as file:
        lines = file.readlines()

        # Process each line
        for line in lines:
            cols = line.split()

            # Skip empty or invalid lines
            if len(cols) != 2:
                continue

            # Accumulate the values
            total_col1 += int(cols[0])
            total_col2 += int(cols[1])
            num_lines += 1

# Calculate the averages
avg_col1 = total_col1 / num_lines
avg_col2 = total_col2 / num_lines

# convert nanoseconds to milliseconds
avg_col1 /= 1000000
avg_col2 /= 1000000

print(f"Average TS: {avg_col2} ms")
print(f"Average TJ: {avg_col1} ms")

# Write the results to log_processed.txt
# with open('log_processed.txt', 'w') as file:
#     file.write(f"Avg TJ: {avg_col1}\n")
#     file.write(f"Avg TS: {avg_col2}\n")
