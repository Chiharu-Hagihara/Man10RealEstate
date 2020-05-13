package red.man10.realestate.menu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import red.man10.realestate.Constants.Companion.isLike
import red.man10.realestate.Constants.Companion.ownerData
import red.man10.realestate.Constants.Companion.regionData
import red.man10.realestate.Plugin

class InventoryMenu(private val pl: Plugin) : Listener {

    val mainMenu = "${pl.prefix}§a§lメインメニュー"
    val bookmark = "${pl.prefix}§a§lいいねしたリスト"

    val ownerMenuClass = OwnerMenu(pl)


    companion object{
        fun IS(pl:Plugin,mateirial: Material, name:String, lore:MutableList<String>, id:String): ItemStack {

            val item = ItemStack(mateirial)
            val meta = item.itemMeta
            meta.persistentDataContainer.set(NamespacedKey(pl,"id"), PersistentDataType.STRING,id)
            meta.setDisplayName(name)
            meta.lore = lore
            item.itemMeta = meta
            return item
        }

        ////////////////////
        //nbttagを取得
        /////////////////////
        fun getId(item:ItemStack,pl:Plugin):String{
            return item.itemMeta!!.persistentDataContainer[NamespacedKey(pl,"id"), PersistentDataType.STRING]?:""
        }

    }

    /////////////////////
    //メインメニュー
    /////////////////////
    fun openMainMenu(p:Player){
        val inv = Bukkit.createInventory(null,9,mainMenu)

        inv.setItem(1,IS(pl,Material.PAPER,"§f§l自分がオーナーの土地を管理する", mutableListOf(),"manage"))
        inv.setItem(4,IS(pl,Material.NETHER_STAR,"§f§lいいねした土地を確認する", mutableListOf(),"bookmark"))
        inv.setItem(7,IS(pl,Material.PAPER,"", mutableListOf(),"unnamed"))

        p.openInventory(inv)
    }

    //////////////////////////
    //いいねした土地の確認
    //////////////////////////
    fun openBookMark(p:Player,first: Int){

        val inv = Bukkit.createInventory(null,54,bookmark)

        val list = isLike[p]!!

        for (i in first .. first+44){

            if (list.size <=i)break

            val d = regionData[list[i]]?:continue

            val icon = IS(pl,Material.PAPER,d.name,mutableListOf(
                    "§e§lID:${i}",
                    "§b§lOwner:${Bukkit.getOfflinePlayer(d.owner_uuid).name}",
                    "§a§lStatus:${d.status}"
            ),i.toString())

            inv.addItem(icon)
        }

        val back = IS(pl,Material.RED_STAINED_GLASS_PANE,"§3§l戻る", mutableListOf(),"back")
        for (i in 45..53){
            inv.setItem(i,back)
        }

        if (inv.getItem(44) != null){

            val next = IS(pl,Material.LIGHT_BLUE_STAINED_GLASS_PANE,"§6§l次のページ", mutableListOf(),"next")
            inv.setItem(51,next)
            inv.setItem(52,next)
            inv.setItem(53,next)

        }

        if (first!=1){
            val previous = IS(pl,Material.LIGHT_BLUE_STAINED_GLASS_PANE,"§6§l前のページ", mutableListOf(),"previous")
            inv.setItem(45,previous)
            inv.setItem(46,previous)
            inv.setItem(47,previous)

        }

        p.openInventory(inv)

    }


    @EventHandler
    fun invEvent(e:InventoryClickEvent){

        val name = e.view.title
        val item = e.currentItem?:return
        val p = e.whoClicked as Player

        //メインメニュ
        if (name == mainMenu){
            e.isCancelled = true
            when(getId(item,pl)){
                "manage"->ownerMenuClass.openOwnerSetting(p,1)
                "bookmark"->openBookMark(p,1)
                else ->{}
            }
        }

        //いいね
        if (name == bookmark){
            e.isCancelled = true
            when(getId(item,pl)){

                "back"->openMainMenu(p)
                "next"->openBookMark(p,getId(e.inventory.getItem(44)!!,pl).toInt()+1)
                "previous"->openBookMark(p,getId(e.inventory.getItem(44)!!,pl).toInt()-45)
                else ->{
                    p.performCommand("mre tp ${(item.lore!!)[0].replace("§e§lID:","").toInt()}")
                }

            }
            return
        }
    }
}