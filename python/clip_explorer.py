import sys
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QApplication, QMainWindow, QListWidget, QGridLayout, QWidget, QLabel
import pandas as pd
import datetime

class Clip:
    def __init__(self, start, stop, category):
        self.start = start
        self.stop = stop
        self.category = category

timestamp_format = "%Y-%m-%d %H:%M:%S.%f"

df = pd.read_csv('../data/sample.csv')
df['timestamp'] = pd.to_datetime(df.timestamp)
start_time = df.timestamp[0]

df = df.iloc[1:, :]

def get_time_offset(x, start=start_time):
    if x == start:
        return x - start
    else:
        return (x - start) - datetime.timedelta(seconds=3)

clips = [Clip(start=get_time_offset(x.timestamp), stop=x.timestamp + datetime.timedelta(seconds=5), category=x['name']) for _, x in df.iterrows()]

class VideoClipBrowser(QMainWindow):
    def __init__(self):
        super().__init__()

        self.initUI()

    def initUI(self):
        self.setWindowTitle('Clip Explorer')
        self.setGeometry(100, 100, 800, 600)

        self.category_list = QListWidget(self)
        self.category_list.setGeometry(10, 10, 200, 580)
        self.category_list.itemClicked.connect(self.category_clicked)

        self.clip_grid = QWidget(self)
        self.clip_grid.setGeometry(170, 10, 620, 580)

        self.load_categories()

    def load_categories(self):
        categories = set(clip.category for clip in clips)
        for category in sorted(categories):
            self.category_list.addItem(category)

    def category_clicked(self, item):
        category = item.text()

        for i in reversed(range(self.clip_grid.layout().count())):
            self.clip_grid.layout().itemAt(i).widget().setParent(None)

        grid = QGridLayout()
        self.clip_grid.setLayout(grid)

        category_clips = [clip for clip in clips if clip.category == category]

        for i, clip in enumerate(category_clips):
            clip_label = QLabel(f'Clip: {clip.start}-{clip.stop}')
            grid.addWidget(clip_label, i // 3, i % 3)

def main():
    app = QApplication(sys.argv)
    ex = VideoClipBrowser()
    ex.show()
    sys.exit(app.exec_())

if __name__ == '__main__':
    main()
