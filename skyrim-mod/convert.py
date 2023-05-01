import pandas as pd

df = pd.read_csv('skyrim_sfx.csv')

def get_category(x):
    if x[:4].isupper():
        return x[:3]
    elif x[:3].isupper():
        return x[:2]
    else:
        print(f"Failed on {x}")
        return 'ERROR'
    
df['category'] = df.name.apply(get_category)

print(df.category.unique())

cats_to_keep = ['DR', 'DRS', 'ITM', 'OBJ', 'TRP', 'MAG', 'QST', 'WPN', 'PHY', 'FX', 'FST', 'VOC']

# df.to_csv('skyrim_sfx_eng.csv', index=False)

df_keep = df[df.category.isin(cats_to_keep)]

df_keep = df_keep[df_keep['category']=='FST']

# 0x00000BEE
ids = ['0x' + x.upper() for x in df_keep['id']]


# int[] myArray = {1, 2, 3, 4, 5};

s = ', '.join(ids)


print(len(df_keep))

def divide_chunks(l, n):
    for i in range(0, len(l), n):
        yield l[i:i + n]

with open('ids.psc', 'w') as f:
    f.write('Function initArrays()\n')
    n = 128
    lists = list(divide_chunks(ids, n))
    for li in range(0, len(lists)):
        arrayName = f'sound_ids{li}'
        for i in range(0, len(lists[li])):
            f.write(f'   {arrayName}[{i}] = {ids[i]}\n')
    f.write('endFunction')