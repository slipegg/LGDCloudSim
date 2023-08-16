# * coding:utf-8_*_
# 作者     :XiangLin
# 创建时间 :2020/10/26 9:14
# 文件     :testsimilar.py
# IDE      :PyCharm

# import os
# import time
# import warnings
import sys
# import config
# import logging
# from gensim.models import Word2Vec
# from gensim.models.word2vec import LineSentence, PathLineSentences
# from pretreatment.pretreatment import PreDeal
# warnings.filterwarnings(action='ignore', category=UserWarning, module='gensim')
# model = Word2Vec.load(r"D:\\model\\word2vec.model")


def similarwords(keyword, tops=5):
#     # 默认获取前10个相似关键词
#     start = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))
#     # print("start execute Word2vec, get similar keywords! Time:" + start +">>>>>>>>>>>>>>>>>>>>>")
#     try:
#         # model = Word2Vec.load(modelpath)
#         words = model.wv.most_similar(keyword, topn=tops)
#     except KeyError:
#         # print("word '%s' not in vocabulary" % keyword)
#         return None
#     end = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))
#     if not words:
#         return None
#         # res = [[item[0], item[1]] for item in words]   # 相似关键词及其相似度
#     res = []
#     for word in words:
#         res.append([word[0], word[1]])
#         print(word[0], "\t", word[1])
    # print("get similar keywords end!................... Time:" + end + ">>>>>>>>>>>>>>>>>>>>>")
    # print(res)
    res = keyword+"xxxxxx"
    print(res)
    return res


if __name__ == '__main__':
    word = sys.argv[1]
    similarwords(word)