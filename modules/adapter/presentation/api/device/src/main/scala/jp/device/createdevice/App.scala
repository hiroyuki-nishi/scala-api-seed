package jp.device.createdevice

import jp.lanscope.domain.Env
import jp.lanscope.domain.device.DeviceRepository
import jp.lanscope.infrastracture.dynamodb.device.DeviceRepositoryOnDynamoDB
import software.amazon.awssdk.regions.Region

class App extends Base {
  protected val deviceRepository: DeviceRepository = new DeviceRepositoryOnDynamoDB(Env("gray"), Region.AP_NORTHEAST_1)
}
