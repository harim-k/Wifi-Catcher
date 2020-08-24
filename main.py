from image_to_string import make_image_to_string_BNW, make_image_to_string


# for index in range(1,5):
#     file_name = index
#     image_to_string(file_name)

# for index in range(1,10):
#     file_name = index
#     make_image_to_string(file_name)
#     make_image_to_string_BNW(file_name)


file_name = input('input file name: ')
make_image_to_string(file_name)
make_image_to_string_BNW(file_name)