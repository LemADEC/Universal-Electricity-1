package universalelectricity.core

import _root_.net.minecraftforge.common.config.Configuration
import java.io.File
import java.util.Map
import java.util.logging.Logger
import universalelectricity.api.UniversalElectricity
import universalelectricity.compatibility.Compatibility
import universalelectricity.compatibility.asm.UniversalTransformer
import cpw.mods.fml.common._
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.relauncher.IFMLCallHook
import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions
import universalelectricity.core.grid.UpdateTicker
import universalelectricity.api.grid.NodeRegistry
import universalelectricity.api.grid.electric.IElectricNode
import universalelectricity.core.grid.electric.ElectricNode
import universalelectricity.compatibility.module.{ModuleBuildCraft, ModuleThermalExpansion, ModuleUniversalElectricity}

@Mod(modid = UniversalElectricity.ID, version = UniversalElectricity.VERSION, name = UniversalElectricity.NAME, dependencies = "before:ForgeMultipart", modLanguage = "scala")
@TransformerExclusions(Array("universalelectricity.compatibility.asm", "universalelectricity.compatibility.asm.template"))
object UELoader extends IFMLLoadingPlugin with IFMLCallHook
{
  /** The Universal Electricity configuration file. */
  var config: Configuration = null

  @SidedProxy(clientSide = "universalelectricity.core.ClientProxy", serverSide = "universalelectricity.core.CommonProxy") var proxy: CommonProxy = null

  @Mod.Metadata(UniversalElectricity.ID)
  var metadata: ModMetadata = null

  val logger = Logger.getLogger(UniversalElectricity.NAME)

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    config = new Configuration(new File(Loader.instance.getConfigDir, "UniversalElectricity.cfg"))
    config.load
    UpdateTicker.useThreads = config.get(Configuration.CATEGORY_GENERAL, "Use multithreading", UpdateTicker.useThreads).getBoolean(UpdateTicker.useThreads)

    Compatibility.register(ModuleUniversalElectricity)

    Array[Compatibility.CompatibilityModule](ModuleThermalExpansion, ModuleBuildCraft).foreach(
      module =>
      {
        module.reciprocal_ratio = config.get("Compatibility", module.moduleName + " Conversion Ratio", module.reciprocal_ratio).getDouble(module.reciprocal_ratio)
        module.ratio = 1d / module.reciprocal_ratio
        module.isEnabled = config.get("Compatibility", "Load " + module.moduleName + " Module", true).getBoolean(true)

        if (module.isEnabled)
        {
          Compatibility.register(module)
        }
      }
    )

    config.save
    proxy.init

    /*
        metadata.modId = UniversalElectricity.ID
        metadata.name = UniversalElectricity.NAME
        metadata.description = "Universal Electricity is a Minecraft modding library that provides an easy, flexible energy framework and compatibility bridges between various energy systems in Minecraft."
        metadata.url = "http://www.universalelectricity.com/"
        metadata.version = UniversalElectricity.VERSION + "." + UniversalElectricity.BUILD_VERSION
        metadata.authorList = List("Calclavia", "DarkCow", "tgame14", "Alex_hawks")
        metadata.credits = "Please visit the website."
        metadata.autogenerated = false
     */
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    if (UpdateTicker.useThreads && !UpdateTicker.isAlive())
    {
      UpdateTicker.start();
    }

    NodeRegistry.register(classOf[IElectricNode], classOf[ElectricNode])
    // TODO: register Thermal Grid
    //UpdateTicker.addNetwork(ResonantEngine.thermalGrid);
  }

  /** Return a list of classes that implements the IClassTransformer interface
    *
    * @return a list of classes that implements the IClassTransformer interface */
  override def getASMTransformerClass: Array[String] = Array[String](classOf[UniversalTransformer].getName)

  /** Return a class name that implements "ModContainer" for injection into the mod list The
    * "getName" function should return a name that other mods can, if need be, depend on.
    * Trivially, this modcontainer will be loaded before all regular mod containers, which means it
    * will be forced to be "immutable" - not susceptible to normal sorting behaviour. All other mod
    * behaviours are available however- this container can receive and handle normal loading events */
  override def getModContainerClass: String = null

  /** Return the class name of an implementor of "IFMLCallHook", that will be run, in the main
    * thread, to perform any additional setup this coremod may require. It will be run
    * <strong>prior</strong> to Minecraft starting, so it CANNOT operate on minecraft itself. The
    * game will deliberately crash if this code is detected to trigger a minecraft class loading
    * (TODO: implement crash ;) ) */
  override def getSetupClass: String =
  {
    return UELoader.getClass.getName
  }

  /** Inject coremod data into this coremod This data includes: "mcLocation" : the location of the
    * minecraft directory, "coremodList" : the list of coremods "coremodLocation" : the file this
    * coremod loaded from, */
  override def injectData(data: Map[String, AnyRef])
  {
  }

  override def call: Void =
  {
    /*
    val asmTETiles: String = System.getProperty("asmTETile")
    val asmBCTiles: String = System.getProperty("asmBCTile")
    val diable: String = System.getProperty("asmUEDsiable")

    if (asmTETiles == null || asmTETiles.equalsIgnoreCase("true") || asmTETiles.equalsIgnoreCase("t"))
    {
      if (asmTETiles == null || asmTETiles.equalsIgnoreCase("true") || asmTETiles.equalsIgnoreCase("t")) TemplateInjectionManager.registerTileTemplate(CompatibilityType.THERMAL_EXPANSION.moduleName, classOf[TemplateTETile], classOf[IEnergyHandler])
      if (asmBCTiles == null || asmBCTiles.equalsIgnoreCase("true") || asmBCTiles.equalsIgnoreCase("t")) TemplateInjectionManager.registerTileTemplate(CompatibilityType.BUILDCRAFT.moduleName, classOf[TemplateBCTile], classOf[IPowerReceptor])
      TemplateInjectionManager.registerItemTemplate(CompatibilityType.THERMAL_EXPANSION.moduleName, classOf[TemplateTEItem], classOf[IEnergyContainerItem])
    }*/

    return null
  }

  override def getAccessTransformerClass: String =
  {
    return null
  }
}