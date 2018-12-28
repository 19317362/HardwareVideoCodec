//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/MainPipeline.h"
#include "../include/Unit.h"

MainPipeline::MainPipeline(string name) {
    pipeline = new HandlerThread(name);
}

MainPipeline::~MainPipeline() {
}

void MainPipeline::release() {
    Message *msg = new Message(EVENT_COMMON_RELEASE, nullptr);
    postEvent(msg);
}

void MainPipeline::postEvent(Message *msg1) {
    if (pipeline) {
        msg1->runnable = [this](Message *msg2) {
            /**
             * @NOTE 不置空的话会出现不可预料的崩溃
             */
            msg2->runnable = nullptr;
            this->dispatch(msg2);
        };
        pipeline->sendMessage(msg1);
    }
}

void MainPipeline::dispatch(Message *msg) {
    for (auto itr = units.cbegin(); itr != units.cend(); itr++) {
        bool ret = (*itr)->dispatch(msg);
    }
    if (EVENT_COMMON_RELEASE == msg->what) {
        clear();
    }
}

void MainPipeline::clear() {
    LOGI("MainPipeline::clear units");
    for (auto unit:units) {
        delete unit;
    }
    units.clear();
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
}

int MainPipeline::registerAnUnit(Unit *unit) {
    unit->setController(this);
    units.push_back(unit);
    return 1;
}
