package com.fuzhou.common.constant;

/**
 * 演出分类常量
 * 定义首页推荐的四大类演出
 */
public final class ShowCategoryConstant {

    // 私有构造函数，防止实例化
    private ShowCategoryConstant() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // 首页推荐的四大类演出（根据实际分类名称调整）
    public static final String CONCERT = "演唱会";      // 演唱会
    public static final String DRAMA = "话剧";         // 话剧
    public static final String MUSIC = "音乐会";        // 音乐会
    public static final String SPORTS = "体育赛事";     // 体育赛事

    /**
     * 获取首页推荐的四大类分类数组
     */
    public static String[] getHomeCategories() {
        return new String[]{CONCERT, DRAMA, MUSIC, SPORTS};
    }
}















